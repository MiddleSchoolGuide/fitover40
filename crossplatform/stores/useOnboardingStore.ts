import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { MMKV } from 'react-native-mmkv';

const storage = new MMKV();

const mmkvStorage = {
  setItem: (name: string, value: string) => storage.set(name, value),
  getItem: (name: string) => storage.getString(name) ?? null,
  removeItem: (name: string) => storage.delete(name),
};

interface OnboardingState {
  hasOnboarded: boolean;
  completeOnboarding: () => void;
}

export const useOnboardingStore = create<OnboardingState>()(
  persist(
    (set) => ({
      hasOnboarded: false,
      completeOnboarding: () => set({ hasOnboarded: true }),
    }),
    {
      name: 'onboarding-storage',
      storage: createJSONStorage(() => mmkvStorage),
    }
  )
);
