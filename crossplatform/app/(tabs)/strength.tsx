import { View, Text, StyleSheet } from 'react-native';
import { MaterialTheme } from '@/constants/colors';

export default function StrengthScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Strength Training</Text>
      <View style={styles.card}>
        <Text style={styles.exercise}>Pushups</Text>
        <Text style={styles.sets}>Target: 3 sets of 12</Text>
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
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
    borderCurve: 'continuous',
  },
  exercise: {
    fontSize: 20,
    fontWeight: '600',
    marginBottom: 8,
  },
  sets: {
    fontSize: 16,
    color: '#666',
  },
});
