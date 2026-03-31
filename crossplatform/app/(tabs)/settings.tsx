import { View, Text, StyleSheet, Pressable } from 'react-native';
import { MaterialTheme } from '@/constants/colors';

export default function SettingsScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Settings</Text>
      <View style={styles.group}>
        <SettingItem label="Units" value="Metric (km)" />
        <SettingItem label="Theme" value="System Default" />
        <SettingItem label="App Version" value="1.0.0" />
      </View>
    </View>
  );
}

function SettingItem({ label, value }: { label: string; value: string }) {
  return (
    <Pressable style={styles.item}>
      <Text style={styles.itemLabel}>{label}</Text>
      <Text style={styles.itemValue}>{value}</Text>
    </Pressable>
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
  group: {
    backgroundColor: 'white',
    borderRadius: 12,
    overflow: 'hidden',
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.05)',
    borderCurve: 'continuous',
  },
  item: {
    padding: 16,
    flexDirection: 'row',
    justifyContent: 'space-between',
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: '#eee',
  },
  itemLabel: {
    fontSize: 16,
    fontWeight: '500',
  },
  itemValue: {
    fontSize: 16,
    color: '#666',
  },
});
