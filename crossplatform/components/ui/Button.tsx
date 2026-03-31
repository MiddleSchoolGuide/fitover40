import React from 'react';
import { Pressable, Text, StyleSheet, ViewStyle } from 'react-native';
import { useAnimatedStyle, withTiming } from 'react-native-reanimated';
import Animated from 'react-native-reanimated';
import { MaterialTheme } from '@/constants/colors';

interface ButtonProps {
  onPress: () => void;
  text: string;
  disabled?: boolean;
  style?: ViewStyle;
}

const AnimatedPressable = Animated.createAnimatedComponent(Pressable);

export const Button = ({ onPress, text, disabled, style }: ButtonProps) => {
  const animatedStyle = useAnimatedStyle(() => ({
    opacity: withTiming(disabled ? 0.5 : 1),
    transform: [{ scale: withTiming(1) }], // Basic placeholder
  }));

  return (
    <AnimatedPressable
      onPress={onPress}
      disabled={disabled}
      style={[styles.button, style, animatedStyle]}
    >
      <Text style={styles.text}>{text}</Text>
    </AnimatedPressable>
  );
};

const styles = StyleSheet.create({
  button: {
    backgroundColor: MaterialTheme.primary,
    paddingVertical: 16,
    paddingHorizontal: 24,
    borderRadius: 30,
    alignItems: 'center',
    justifyContent: 'center',
  },
  text: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
});
