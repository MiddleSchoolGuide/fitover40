import { View, Text, StyleSheet } from 'react-native';
import { LegendList } from '@legendapp/list';
import { MaterialTheme } from '@/constants/colors';

const MOCK_HISTORY = [
  { id: '1', date: '2026-03-31', workout: 'Running - Beginner', duration: '20 min' },
  { id: '2', date: '2026-03-29', workout: 'Strength - Upper Body', duration: '45 min' },
  { id: '3', date: '2026-03-27', workout: 'Running - Intermediate', duration: '30 min' },
];

export default function HistoryScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Workout History</Text>
      <LegendList
        data={MOCK_HISTORY}
        estimatedItemSize={80}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <View style={styles.item}>
            <Text style={styles.itemTitle}>{item.workout}</Text>
            <Text style={styles.itemSubtitle}>{item.date} • {item.duration}</Text>
          </View>
        )}
      />
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
  item: {
    backgroundColor: 'white',
    padding: 16,
    borderRadius: 12,
    marginBottom: 12,
    boxShadow: '0 2px 4px rgba(0, 0, 0, 0.05)',
    borderCurve: 'continuous',
  },
  itemTitle: {
    fontSize: 18,
    fontWeight: '600',
  },
  itemSubtitle: {
    fontSize: 14,
    color: '#666',
    marginTop: 4,
  },
});
