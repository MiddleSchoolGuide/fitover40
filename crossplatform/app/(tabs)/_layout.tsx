import { NativeTabs, Label } from 'expo-router/unstable-native-tabs';
import { MaterialTheme } from '@/constants/colors';

export default function TabLayout() {
  return (
    <NativeTabs
      screenOptions={{
        activeTintColor: MaterialTheme.primary,
        inactiveTintColor: MaterialTheme.onSurfaceVariant,
      }}
    >
      <NativeTabs.Trigger name="index">
        <Label>History</Label>
        <NativeTabs.Trigger.Icon sf="clock.fill" md="history" />
      </NativeTabs.Trigger>

      <NativeTabs.Trigger name="running">
        <Label>Running</Label>
        <NativeTabs.Trigger.Icon sf="figure.run" md="directions-run" />
      </NativeTabs.Trigger>

      <NativeTabs.Trigger name="strength">
        <Label>Strength</Label>
        <NativeTabs.Trigger.Icon sf="dumbbell.fill" md="fitness-center" />
      </NativeTabs.Trigger>

      <NativeTabs.Trigger name="settings">
        <Label>Settings</Label>
        <NativeTabs.Trigger.Icon sf="gear" md="settings" />
      </NativeTabs.Trigger>
    </NativeTabs>
  );
}
