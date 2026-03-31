import { Stack } from 'expo-router';
import { useEffect } from 'react';
import { useOnboardingStore } from '@/stores/useOnboardingStore';
import { useRouter, useSegments } from 'expo-router';

export default function RootLayout() {
  const { hasOnboarded } = useOnboardingStore();
  const segments = useSegments();
  const router = useRouter();

  useEffect(() => {
    const inAuthGroup = segments[0] === 'onboarding';

    if (!hasOnboarded && !inAuthGroup) {
      // Redirect to onboarding if not onboarded
      router.replace('/onboarding');
    } else if (hasOnboarded && inAuthGroup) {
      // Redirect to tabs if already onboarded
      router.replace('/(tabs)');
    }
  }, [hasOnboarded, segments]);

  return (
    <Stack screenOptions={{ headerShown: false }}>
      <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
      <Stack.Screen name="onboarding" options={{ presentation: 'fullScreenModal' }} />
    </Stack>
  );
}
