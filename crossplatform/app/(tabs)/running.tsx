import { View, Text, StyleSheet } from 'react-native';
import { MaterialTheme } from '@/constants/colors';

export default function RunningScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Interval Running</Text>
      <View style={styles.card}>
        <Text style={styles.status}>Ready to Start?</Text>
        <Text style={styles.plan}>Beginner Plan - Day 1</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: MaterialTheme.background,
    paddingTop: 60,
    paddingHorizontal: 16,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    marginBottom: 20,
    color: MaterialTheme.primary,
  },
  card: {
    backgroundColor: 'white',
    padding: 24,
    borderRadius: 16,
    alignItems: 'center',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
    borderCurve: 'continuous',
  },
  status: {
    fontSize: 20,
    fontWeight: '600',
    marginBottom: 8,
  },
  plan: {
    fontSize: 16,
    color: '#666',
  },
});
