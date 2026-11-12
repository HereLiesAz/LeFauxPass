#!/usr/bin/env python3
"""Convert every animated WebP frame to lossless RGBA data for Compose Canvas.

The result contains *pixels*, not the original WebP. The Compose renderer draws
consecutive equal pixels as rectangles, so colors can be remapped independently.
No frames are dropped, quantized, resampled, or interpolated.
"""
from __future__ import annotations

import argparse
import hashlib
from pathlib import Path
import struct

from PIL import Image
import lz4.block

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "LeFauxPassRN/assets/animation.webp"
OUTPUT = ROOT / "shared/src/commonMain/composeResources/files/animation-frames.lz4"
MAGIC = b"CAF1"
HEADER = struct.Struct("<4sHHI")
FRAME = struct.Struct("<HI")


def source_frames(path: Path):
    with Image.open(path) as animation:
        width, height = animation.size
        for index in range(animation.n_frames):
            animation.seek(index)
            duration = int(animation.info.get("duration", 100))
            if not 1 <= duration <= 65535:
                raise ValueError(f"Invalid duration in frame {index}: {duration}")
            rgba = animation.convert("RGBA").tobytes()
            if len(rgba) != width * height * 4:
                raise AssertionError(f"Unexpected RGBA size in frame {index}")
            yield (width, height, duration, rgba)


def encode(path: Path):
    frames = list(source_frames(path))
    if not frames:
        raise ValueError("Animation has no frames")
    width, height, _, _ = frames[0]
    if width > 65535 or height > 65535:
        raise ValueError("Dimensions exceed binary format")
    data = bytearray(HEADER.pack(MAGIC, width, height, len(frames)))
    for fw, fh, duration, rgba in frames:
        if (fw, fh) != (width, height):
            raise ValueError("Frame dimensions differ")
        packed = lz4.block.compress(rgba, mode="high_compression",
                                    compression=9, store_size=False)
        data += FRAME.pack(duration, len(packed))
        data += packed
    return bytes(data), frames


def validate(data: bytes, expected: list):
    magic, width, height, count = HEADER.unpack_from(data)
    assert magic == MAGIC
    assert count == len(expected)
    assert (width, height) == expected[0][:2]
    offset = HEADER.size
    total_duration = 0
    for i, (fw, fh, duration, source) in enumerate(expected):
        actual_duration, length = FRAME.unpack_from(data, offset)
        offset += FRAME.size
        frame = lz4.block.decompress(data[offset:offset + length],
                                      uncompressed_size=width * height * 4)
        offset += length
        # Independently sourced reference bytes, not a copy of the decoder's output.
        assert frame == source, f"Frame {i} differs from WebP"
        assert duration == actual_duration
        total_duration += duration
    assert offset == len(data), "Unexpected trailing bytes"
    return total_duration


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, default=SOURCE)
    parser.add_argument("--output", type=Path, default=OUTPUT)
    parser.add_argument("--check", action="store_true",
                        help="Compare committed data to a fresh WebP decode")
    args = parser.parse_args()
    encoded, expected = encode(args.source)
    duration = validate(encoded, expected)
    if args.check:
        assert args.output.read_bytes() == encoded, "Generated frames are stale"
    else:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_bytes(encoded)
    print(f"WebP: {expected[0][0]}x{expected[0][1]}, {len(expected)} frames, "
          f"{duration} ms; exact RGBA round trip")
    print(f"Compose data: {len(encoded)} bytes, sha256 "
          f"{hashlib.sha256(encoded).hexdigest()}")
    print("Checked committed data" if args.check else f"Wrote {args.output}")


if __name__ == "__main__":
    main()
