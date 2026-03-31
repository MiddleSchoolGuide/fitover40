import { View, Text, StyleSheet, Pressable, ScrollView } from 'react-native';
import { useOnboardingStore } from '@/stores/useOnboardingStore';
import { MaterialTheme } from '@/constants/colors';
import { SafeAreaView } from 'react-native-safe-area-context';

export default function OnboardingScreen() {
  const { completeOnboarding } = useOnboardingStore();

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.title}>Welcome to FitOver40</Text>
        <Text style={styles.description}>
          Fitness is a lifelong journey. This app helps you track your interval runs and strength training specifically designed for the 40+ athlete.
        </Text>

        <View style={styles.disclaimerBox}>
          <Text style={styles.disclaimerTitle}>Health Disclaimer</Text>
          <Text style={styles.disclaimerText}>
            Before starting any new exercise program, consult your physician or healthcare provider, especially if you have any pre-existing medical conditions. This app does not provide medical advice.
          </Text>
        </View>

        <Pressable 
          style={({ pressed }) => [
            styles.button,
            { opacity: pressed ? 0.8 : 1 }
          ]} 
          onPress={completeOnboarding}
        >
          <Text style={styles.buttonText}>I Understand & Get Started</Text>
        </Pressable>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  content: {
    padding: 24,
    alignItems: 'center',
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: MaterialTheme.primary,
    textAlign: 'center',
    marginTop: 40,
    marginBottom: 16,
  },
  description: {
    fontSize: 18,
    textAlign: 'center',
    color: '#444',
    marginBottom: 40,
    lineHeight: 26,
  },
  disclaimerBox: {
    backgroundColor: '#f8f8f8',
    padding: 20,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#eee',
    marginBottom: 40,
  },
  disclaimerTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 8,
    color: '#d32f2f',
  },
  disclaimerText: {
    fontSize: 14,
    color: '#666',
    lineHeight: 20,
  },
  button: {
    backgroundColor: MaterialTheme.primary,
    paddingVertical: 16,
    paddingHorizontal: 32,
    borderRadius: 30,
    width: '100%',
    alignItems: 'center',
  },
  buttonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: 'bold',
  },
});
