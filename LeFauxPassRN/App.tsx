import React, { useState, useEffect } from 'react';
import {
  StyleSheet,
  Text,
  View,
  Image,
  StatusBar,
  TouchableOpacity,
  ScrollView
} from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';

const TICKET_DURATION_HOURS = 1;
const TICKET_DURATION_MINUTES = 56;
const EXPIRATION_KEY = 'ticketExpirationTime';

function App(): React.JSX.Element {
  const [currentTime, setCurrentTime] = useState(new Date());
  const [expirationTime, setExpirationTime] = useState<string | null>(null);

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    const checkExpiration = async () => {
      try {
        let storedExpiration = await AsyncStorage.getItem(EXPIRATION_KEY);

        if (!storedExpiration || new Date(storedExpiration) < new Date()) {
          const now = new Date();
          now.setHours(now.getHours() + TICKET_DURATION_HOURS);
          now.setMinutes(now.getMinutes() + TICKET_DURATION_MINUTES);
          storedExpiration = now.toISOString();
          await AsyncStorage.setItem(EXPIRATION_KEY, storedExpiration);
        }

        setExpirationTime(storedExpiration);
      } catch (e) {
        console.error("Failed to load expiration time", e);
      }
    };

    checkExpiration();
  }, []);

  const formatTime = (date: Date) => {
    return date.toLocaleTimeString('en-US', {
      hour: 'numeric',
      minute: '2-digit',
      second: '2-digit',
      hour12: true
    });
  };

  const formatExpiration = (isoString: string) => {
    const date = new Date(isoString);
    const dateStr = date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
    const timeStr = date.toLocaleTimeString('en-US', {
      hour: 'numeric',
      minute: '2-digit',
      hour12: true
    });
    return `Expires ${dateStr}, ${timeStr}`;
  };

  return (
    <View style={styles.container}>
        <StatusBar backgroundColor="#303235" barStyle="light-content" />

        {/* Top Bar */}
        <View style={styles.topBar}>
            <TouchableOpacity style={styles.navIcon}>
                <Text style={styles.navIconText}>{'\u2190'}</Text>
            </TouchableOpacity>
            <View style={styles.title} />
            <TouchableOpacity style={styles.navIcon}>
                <Text style={[styles.navIconText, styles.infoButton]}>{'\u24D8'}</Text>
            </TouchableOpacity>
        </View>

        <ScrollView contentContainerStyle={styles.ticketContent}>
            <View style={styles.ticketHeader}>
                <Text style={styles.headerTitle}>RTA</Text>
                <Text style={styles.headerSubtitle}>Show operator your ticket</Text>
            </View>

            <View style={styles.animationContainer}>
                <Image
                    source={require('./assets/animation.webp')}
                    style={styles.animationImage}
                />
            </View>

            <Text style={styles.liveClock}>
                {formatTime(currentTime)}
            </Text>

            <View style={styles.ticketInfoCard}>
                <Text style={styles.ticketType}>Adult Single Ride, Bus & Streetcar</Text>
                <Text style={styles.location}>New Orleans, LA</Text>
                <Text style={styles.expiration}>
                    {expirationTime ? formatExpiration(expirationTime) : 'Loading...'}
                </Text>
            </View>
        </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  topBar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#303235',
    padding: 10,
    height: 56,
  },
  navIcon: {
    padding: 8,
  },
  navIconText: {
    color: 'white',
    fontSize: 24,
  },
  title: {
    // empty
  },
  infoButton: {
    color: '#B865D0',
  },
  ticketContent: {
    flexGrow: 1,
    padding: 24,
    alignItems: 'center',
  },
  ticketHeader: {
    alignItems: 'center',
    marginBottom: 20,
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#000000',
  },
  headerSubtitle: {
    fontSize: 16,
    color: '#757575',
    marginTop: 4,
  },
  animationContainer: {
    marginVertical: 20,
    alignItems: 'center',
    width: '100%',
  },
  animationImage: {
    width: 200,
    height: 200,
    resizeMode: 'contain',
  },
  liveClock: {
    fontSize: 48,
    fontWeight: 'bold',
    marginVertical: 24,
    color: '#000000',
  },
  ticketInfoCard: {
    width: '100%',
    backgroundColor: '#FFFFFF',
    borderColor: '#e0e0e0',
    borderWidth: 1,
    borderRadius: 12,
    padding: 16,
    marginTop: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
    alignItems: 'flex-start',
  },
  ticketType: {
    fontWeight: 'bold',
    fontSize: 18,
    color: '#000000',
  },
  location: {
    fontSize: 14,
    marginVertical: 4,
    marginBottom: 24,
    color: '#000000',
  },
  expiration: {
    fontSize: 16,
    color: '#000000',
  },
});

export default App;
