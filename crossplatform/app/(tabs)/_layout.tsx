import { NativeTabs } from 'expo-router/unstable-native-tabs';
import { MaterialTheme } from '@/constants/colors';

export default function TabLayout() {
  return (
    <NativeTabs
      iconColor={{
        selected: MaterialTheme.primary,
        default: MaterialTheme.onSurfaceVariant,
      }}
      labelStyle={{
        selected: { color: MaterialTheme.primary },
        default: { color: MaterialTheme.onSurfaceVariant },
      }}
    >
      <NativeTabs.Trigger name="index">
        <NativeTabs.Trigger.Label>History</NativeTabs.Trigger.Label>
        <NativeTabs.Trigger.Icon sf="clock.fill" md="history" />
      </NativeTabs.Trigger>

      <NativeTabs.Trigger name="running">
        <NativeTabs.Trigger.Label>Running</NativeTabs.Trigger.Label>
        <NativeTabs.Trigger.Icon sf="figure.run" md="directions_run" />
      </NativeTabs.Trigger>

      <NativeTabs.Trigger name="strength">
        <NativeTabs.Trigger.Label>Strength</NativeTabs.Trigger.Label>
        <NativeTabs.Trigger.Icon sf="dumbbell.fill" md="fitness_center" />
      </NativeTabs.Trigger>

      <NativeTabs.Trigger name="settings">
        <NativeTabs.Trigger.Label>Settings</NativeTabs.Trigger.Label>
        <NativeTabs.Trigger.Icon sf="gear" md="settings" />
      </NativeTabs.Trigger>
    </NativeTabs>
  );
}
