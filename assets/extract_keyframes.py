# We'll sample every frame (since ~60fps, manageable), detect distinct colored regions to estimate shapes.

import cv2
import json
import numpy as np
import os
from collections import defaultdict


def detect_shapes_in_frame(frame_bgr):
    """Return a list of (shape_type, center_x, center_y, width, height, rotation_deg, color_bgr)"""
    results = []
    frame_h, frame_w = frame_bgr.shape[:2]

    # Convert to HSV for easier color segmentation
    hsv = cv2.cvtColor(frame_bgr, cv2.COLOR_BGR2HSV)

    # We'll do contour detection for all visible areas
    gray = cv2.cvtColor(frame_bgr, cv2.COLOR_BGR2GRAY)
    _, mask = cv2.threshold(gray, 10, 255, cv2.THRESH_BINARY)

    contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    for cnt in contours:
        if cv2.contourArea(cnt) < 30:  # skip tiny
            continue

        rect = cv2.minAreaRect(cnt)
        (cx, cy), (w, h), angle = rect
        # Get mean color in contour
        mask_contour = np.zeros(gray.shape, dtype=np.uint8)
        cv2.drawContours(mask_contour, [cnt], -1, 255, -1)
        mean_color = cv2.mean(frame_bgr, mask=mask_contour)[:3]  # BGR

        # crude shape detection: aspect ratio, vertices
        approx = cv2.approxPolyDP(cnt, 0.04 * cv2.arcLength(cnt, True), True)
        if len(approx) == 3:
            shape_type = "triangle"
        elif len(approx) >= 8:
            shape_type = "circle"
        else:
            shape_type = "rect"

        results.append(
            (shape_type, cx / frame_w, cy / frame_h, w / frame_w, h / frame_h, angle, mean_color))

    return results


# --- IMPORTANT: EDIT THIS LINE ---
# Replace this with the full, absolute path to your video file.
# Example for Windows: "C:/Users/YourUser/Videos/my_video.mp4"
# Example for Mac/Linux: "/home/YourUser/Videos/my_video.mp4"
video_path = "G:/My Drive/LAFauxPass/assets/screen-20230622-110513~2 (1).mp4"  # <-- EDIT THIS

# --- Analyze all frames ---
all_frames_shapes = []
cap = cv2.VideoCapture(video_path)

# --- NEW: Check if video opened successfully ---
if not cap.isOpened():
    print(f"Error: Could not open video file at path: {video_path}")
    print("Please check that the file exists and the path is correct.")
else:
    while True:
        ret, frame = cap.read()
        if not ret:
            break
        timestamp_ms = cap.get(cv2.CAP_PROP_POS_MSEC)
        shapes = detect_shapes_in_frame(frame)
        all_frames_shapes.append((timestamp_ms, shapes))
    cap.release()

    # --- Prepare data for JSON serialization ---
    output_data = []
    for timestamp, shapes_in_frame in all_frames_shapes:
        frame_data = {
            "timestamp_ms": timestamp,
            "shapes": []
        }
        for shape_type, cx, cy, w, h, angle, color in shapes_in_frame:
            frame_data["shapes"].append({
                "type": shape_type,
                "cx": cx,
                "cy": cy,
                "w": w,
                "h": h,
                "angle": angle,
                "color_bgr": [int(c) for c in color]
            })
        output_data.append(frame_data)

    # --- Save the data to a JSON file ---
    script_dir = os.path.dirname(os.path.abspath(__file__))
    output_path = os.path.join(script_dir, "keyframes.json")

    with open(output_path, "w") as f:
        json.dump(output_data, f, indent=4)

    if not output_data:
        print(
            "Warning: The video was processed, but no shapes were detected. The output file may be empty or contain no shape data.")
    else:
        print(f"Successfully saved keyframe data to {output_path}")
