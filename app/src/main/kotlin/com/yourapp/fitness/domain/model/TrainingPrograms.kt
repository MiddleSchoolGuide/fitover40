package com.yourapp.fitness.domain.model

object TrainingPrograms {

    fun runningPlan(level: TrainingLevel): RunPlan =
        when (level) {
            TrainingLevel.BeginnerFirstTimeEver -> RunPlan(
                name = "Starter Intervals",
                warmUpMinutes = 5,
                runSeconds = 30,
                walkSeconds = 90,
                sets = 8,
                coolDownMinutes = 5,
                isPreset = true,
                summary = "Short jog intervals with plenty of walking recovery.",
                effortCue = "Keep every jog easy enough that you can still speak in short sentences."
            )
            TrainingLevel.BeginnerBeenAWhile -> RunPlan(
                name = "Return-to-Run Intervals",
                warmUpMinutes = 5,
                runSeconds = 60,
                walkSeconds = 90,
                sets = 8,
                coolDownMinutes = 5,
                isPreset = true,
                summary = "A gentle re-entry plan for rebuilding rhythm and durability.",
                effortCue = "Stay conversational and finish feeling like you could do one more round."
            )
            TrainingLevel.IntermediateBeenAWhile -> RunPlan(
                name = "Controlled Comeback Intervals",
                warmUpMinutes = 6,
                runSeconds = 120,
                walkSeconds = 60,
                sets = 8,
                coolDownMinutes = 5,
                isPreset = true,
                summary = "Longer run segments with enough recovery to rebuild fitness safely.",
                effortCue = "Run smooth and steady, not hard."
            )
            TrainingLevel.Intermediate -> RunPlan(
                name = "Steady Intervals",
                warmUpMinutes = 6,
                runSeconds = 240,
                walkSeconds = 60,
                sets = 6,
                coolDownMinutes = 5,
                isPreset = true,
                summary = "Structured aerobic work for runners with an established base.",
                effortCue = "Lock into a repeatable pace that feels strong but sustainable."
            )
            TrainingLevel.Advanced -> RunPlan(
                name = "Advanced Endurance Blocks",
                warmUpMinutes = 8,
                runSeconds = 480,
                walkSeconds = 60,
                sets = 4,
                coolDownMinutes = 6,
                isPreset = true,
                summary = "Longer sustained efforts for experienced runners.",
                effortCue = "Hold controlled effort and avoid surging early."
            )
            TrainingLevel.HighlyAdvanced -> RunPlan(
                name = "Performance Blocks",
                warmUpMinutes = 10,
                runSeconds = 900,
                walkSeconds = 60,
                sets = 3,
                coolDownMinutes = 8,
                isPreset = true,
                summary = "Demanding endurance work for highly trained runners.",
                effortCue = "Stay relaxed under load and finish each block with good form."
            )
        }

    fun strengthPlanName(level: TrainingLevel): String =
        when (level) {
            TrainingLevel.BeginnerFirstTimeEver -> "Strength Foundations"
            TrainingLevel.BeginnerBeenAWhile -> "Strength Restart"
            TrainingLevel.IntermediateBeenAWhile -> "Intermediate Rebuild"
            TrainingLevel.Intermediate -> "Intermediate Strength"
            TrainingLevel.Advanced -> "Advanced Strength"
            TrainingLevel.HighlyAdvanced -> "Highly Advanced Strength"
        }

    fun strengthExercises(level: TrainingLevel): List<ExercisePlan> =
        when (level) {
            TrainingLevel.BeginnerFirstTimeEver -> beginnerFirstTimeExercises()
            TrainingLevel.BeginnerBeenAWhile -> beginnerReturnExercises()
            TrainingLevel.IntermediateBeenAWhile -> intermediateReturnExercises()
            TrainingLevel.Intermediate -> intermediateExercises()
            TrainingLevel.Advanced -> advancedExercises()
            TrainingLevel.HighlyAdvanced -> highlyAdvancedExercises()
        }

    private fun beginnerFirstTimeExercises() = listOf(
        ExercisePlan(
            name = "Glute Bridges",
            sets = 2,
            reps = 10,
            restSeconds = 45,
            isJointFriendly = true,
            summary = "Build hip strength without loading the knees or lower back heavily.",
            beginnerSetup = "Lie on your back with knees bent, feet flat, and arms relaxed by your sides.",
            formCues = listOf(
                "Press through your heels and lift your hips until your body forms a straight line from shoulders to knees.",
                "Pause for a second at the top and squeeze your glutes.",
                "Lower slowly instead of dropping back down."
            ),
            easierOption = "Do fewer reps or use a smaller range if you feel shaky.",
            mediaSourceName = "ACE Fitness",
            videoUrl = "https://www.acefitness.org/resources/everyone/exercise-library/127/glute-bridge/",
            illustration = ExerciseIllustration.GluteBridge
        ),
        ExercisePlan(
            name = "Bird Dog",
            sets = 2,
            reps = 8,
            restSeconds = 30,
            isJointFriendly = true,
            summary = "Improve balance and core control with slow, low-impact movement.",
            beginnerSetup = "Start on hands and knees with your hands under shoulders and knees under hips.",
            formCues = listOf(
                "Reach one arm forward and the opposite leg back without twisting your torso.",
                "Keep your neck long and eyes down.",
                "Return to the start with control before switching sides."
            ),
            easierOption = "Lift only the arm or only the leg if balancing both feels too difficult.",
            mediaSourceName = "ACE Fitness",
            videoUrl = "https://www.acefitness.org/resources/everyone/exercise-library/14/bird-dog/",
            illustration = ExerciseIllustration.BirdDog
        ),
        ExercisePlan(
            name = "Wall Pushups",
            sets = 2,
            reps = 8,
            restSeconds = 60,
            isJointFriendly = true,
            summary = "Practice pushing strength at an angle that is easier than floor pushups.",
            beginnerSetup = "Stand facing a wall, hands at chest height, and walk your feet back until your body is on a slight diagonal.",
            formCues = listOf(
                "Keep your body in one straight line as you bend your elbows.",
                "Bring your chest toward the wall, not your chin.",
                "Push the wall away and finish with soft elbows."
            ),
            easierOption = "Stand closer to the wall to reduce the difficulty.",
            mediaSourceName = "Mayo Clinic",
            videoUrl = "https://www.mayoclinic.org/healthy-lifestyle/fitness/multimedia/push-up/vid-20084656",
            illustration = ExerciseIllustration.WallPushUp
        ),
        ExercisePlan(
            name = "Bodyweight Squats (to Chair)",
            sets = 2,
            reps = 10,
            restSeconds = 60,
            isJointFriendly = true,
            summary = "Teach a safe sit-to-stand pattern that strengthens legs and hips.",
            beginnerSetup = "Stand in front of a sturdy chair with feet about hip-width apart and toes slightly turned out.",
            formCues = listOf(
                "Reach your hips back toward the chair first.",
                "Keep your chest proud and knees tracking over your toes.",
                "Lightly tap the chair, then stand back up through your whole foot."
            ),
            easierOption = "Use the chair arms or add a cushion to make the range shorter.",
            mediaSourceName = "Mayo Clinic",
            videoUrl = "https://www.mayoclinic.org/healthy-lifestyle/fitness/multimedia/squat/vid-20084662",
            illustration = ExerciseIllustration.ChairSquat
        ),
        ExercisePlan(
            name = "Seated Resistance Band Rows",
            sets = 2,
            reps = 10,
            restSeconds = 45,
            isJointFriendly = true,
            summary = "Strengthen upper back muscles that help posture and shoulder comfort.",
            beginnerSetup = "Sit tall with legs extended or slightly bent, loop the band around your feet, and hold one end in each hand.",
            formCues = listOf(
                "Pull your elbows back close to your sides.",
                "Think about squeezing your shoulder blades gently together.",
                "Return slowly so the band does not snap you forward."
            ),
            easierOption = "Use a lighter band or hold the band with less tension to start.",
            mediaSourceName = "Mayo Clinic",
            videoUrl = "https://www.mayoclinic.org/healthy-lifestyle/fitness/multimedia/seated-row-with-resistance-tubing/vid-20084690",
            illustration = ExerciseIllustration.BandRow
        )
    )

    private fun beginnerReturnExercises() = listOf(
        ExercisePlan(
            name = "Glute Bridges",
            sets = 3,
            reps = 12,
            restSeconds = 45,
            isJointFriendly = true,
            summary = "Rebuild hip drive and control before moving to heavier lower-body work.",
            beginnerSetup = "Lie on your back with feet flat, knees bent, and ribs relaxed.",
            formCues = listOf(
                "Drive through your heels.",
                "Lift until hips and thighs line up without arching your back.",
                "Lower slowly and reset each rep."
            ),
            easierOption = "Pause at the top for less time or reduce the reps if fatigue changes your form.",
            mediaSourceName = "ACE Fitness",
            videoUrl = "https://www.acefitness.org/resources/everyone/exercise-library/127/glute-bridge/",
            illustration = ExerciseIllustration.GluteBridge
        ),
        ExercisePlan(
            name = "Bird Dog",
            sets = 3,
            reps = 10,
            restSeconds = 30,
            isJointFriendly = true,
            summary = "Rebuild trunk stiffness and balance after time away from training.",
            beginnerSetup = "Set up on all fours with a neutral spine and hands pressing firmly into the floor.",
            formCues = listOf(
                "Reach long instead of lifting high.",
                "Keep your pelvis level.",
                "Move slowly enough that nothing wobbles."
            ),
            easierOption = "Shorten the reach or alternate just the arms and legs separately.",
            mediaSourceName = "ACE Fitness",
            videoUrl = "https://www.acefitness.org/resources/everyone/exercise-library/14/bird-dog/",
            illustration = ExerciseIllustration.BirdDog
        ),
        ExercisePlan(
            name = "Incline Pushups",
            sets = 3,
            reps = 8,
            restSeconds = 60,
            isJointFriendly = true,
            summary = "Build upper-body pushing strength on a countertop, bench, or sturdy table.",
            beginnerSetup = "Place your hands on a stable elevated surface and walk your feet back into a straight-body plank.",
            formCues = listOf(
                "Lower your chest between your hands.",
                "Keep your ribs and hips moving together.",
                "Press back up without locking out hard."
            ),
            easierOption = "Use a higher surface if the current angle feels too demanding.",
            mediaSourceName = "Mayo Clinic",
            videoUrl = "https://www.mayoclinic.org/healthy-lifestyle/fitness/multimedia/push-up/vid-20084656"
        ),
        ExercisePlan(
            name = "Bodyweight Squats (to Chair)",
            sets = 3,
            reps = 12,
            restSeconds = 60,
            isJointFriendly = true,
            summary = "Strengthen legs with a reliable sit-to-stand pattern.",
            beginnerSetup = "Stand in front of a chair with a comfortable stance and toes slightly turned out.",
            formCues = listOf(
                "Send the hips back before the knees move forward.",
                "Stay tall through the chest.",
                "Stand up by pressing evenly through the whole foot."
            ),
            easierOption = "Use the chair arms or reduce the depth.",
            mediaSourceName = "Mayo Clinic",
            videoUrl = "https://www.mayoclinic.org/healthy-lifestyle/fitness/multimedia/squat/vid-20084662",
            illustration = ExerciseIllustration.ChairSquat
        ),
        ExercisePlan(
            name = "Seated Resistance Band Rows",
            sets = 3,
            reps = 12,
            restSeconds = 45,
            isJointFriendly = true,
            summary = "Rebuild posture and pulling strength with a low-impact row.",
            beginnerSetup = "Anchor the band at your feet, sit tall, and start with light tension.",
            formCues = listOf(
                "Lead with your elbows.",
                "Keep shoulders down away from your ears.",
                "Control the return."
            ),
            easierOption = "Reduce band tension or cut two reps from each set.",
            mediaSourceName = "Mayo Clinic",
            videoUrl = "https://www.mayoclinic.org/healthy-lifestyle/fitness/multimedia/seated-row-with-resistance-tubing/vid-20084690",
            illustration = ExerciseIllustration.BandRow
        )
    )

    private fun intermediateReturnExercises() = listOf(
        baseExercise(
            name = "Goblet Squats",
            sets = 3,
            reps = 10,
            restSeconds = 60,
            summary = "Reintroduce loaded leg strength with a manageable front-loaded squat.",
            beginnerSetup = "Hold one dumbbell or kettlebell at chest height and stand with feet just outside hip width.",
            formCues = listOf("Brace before you descend.", "Keep elbows angled down.", "Drive up through the middle of the foot."),
            easierOption = "Use bodyweight only or shorten the depth until your hips loosen up."
        ),
        baseExercise(
            name = "Romanian Deadlifts",
            sets = 3,
            reps = 10,
            restSeconds = 60,
            summary = "Restore posterior-chain strength and hamstring tolerance.",
            beginnerSetup = "Stand tall with dumbbells close to your thighs and soften your knees slightly.",
            formCues = listOf("Push hips back.", "Keep the weights close to your legs.", "Stand tall by driving hips forward."),
            easierOption = "Use lighter weights and stop at mid-shin instead of reaching lower."
        ),
        baseExercise(
            name = "Step-Ups",
            sets = 3,
            reps = 8,
            restSeconds = 45,
            summary = "Build single-leg strength and balance with controlled height.",
            beginnerSetup = "Face a low step or box and plant the whole lead foot on top.",
            formCues = listOf("Lean slightly forward.", "Push through the lead leg.", "Lower under control rather than dropping."),
            easierOption = "Use a lower step or hold a rail lightly for balance."
        ),
        baseExercise(
            name = "Incline Pushups",
            sets = 3,
            reps = 10,
            restSeconds = 60,
            summary = "Rebuild upper-body pressing strength with a stable incline.",
            beginnerSetup = "Use a bench, box, or countertop and set your body in a straight line.",
            formCues = listOf("Lower chest first.", "Keep elbows about 45 degrees from your sides.", "Finish with strong but smooth presses."),
            easierOption = "Raise the surface height if the reps slow down too early."
        ),
        baseExercise(
            name = "Single-Arm Dumbbell Rows",
            sets = 3,
            reps = 10,
            restSeconds = 45,
            summary = "Bring back upper-back pulling strength and trunk control.",
            beginnerSetup = "Support one hand on a bench or chair and let the other arm hang with the weight.",
            formCues = listOf("Pull elbow toward your hip.", "Keep torso steady.", "Lower the weight all the way with control."),
            easierOption = "Use a lighter dumbbell or reduce the reps to keep form crisp."
        )
    )

    private fun intermediateExercises() = listOf(
        baseExercise(
            name = "Goblet Squats",
            sets = 4,
            reps = 10,
            restSeconds = 60,
            summary = "Solid lower-body strength builder with an upright torso demand.",
            beginnerSetup = "Hold a weight at your chest and set your feet in your strongest squat stance.",
            formCues = listOf("Brace before each rep.", "Keep pressure through the whole foot.", "Stand up hard without losing position."),
            easierOption = "Use a lighter weight or cut the set at eight reps if position slips."
        ),
        baseExercise(
            name = "Reverse Lunges",
            sets = 3,
            reps = 8,
            restSeconds = 60,
            summary = "Train single-leg strength and hip stability with less knee stress than forward lunges.",
            beginnerSetup = "Stand tall, step one leg back, and stay balanced over the front foot.",
            formCues = listOf("Lower straight down.", "Keep front knee tracking over toes.", "Drive through the front leg to return."),
            easierOption = "Use bodyweight only or hold onto a support lightly."
        ),
        baseExercise(
            name = "Dumbbell Romanian Deadlifts",
            sets = 4,
            reps = 8,
            restSeconds = 75,
            summary = "Develop posterior-chain strength and hinge mechanics.",
            beginnerSetup = "Hold dumbbells by your sides and unlock your knees slightly.",
            formCues = listOf("Push hips back first.", "Maintain a long spine.", "Stand tall by squeezing glutes."),
            easierOption = "Reduce load or stop when you feel the stretch instead of reaching lower."
        ),
        baseExercise(
            name = "Pushups",
            sets = 3,
            reps = 8,
            restSeconds = 60,
            summary = "Build full-body pressing strength with bodyweight.",
            beginnerSetup = "Set a plank with hands under shoulders and feet about hip-width apart.",
            formCues = listOf("Keep a straight line from head to heel.", "Lower chest and hips together.", "Press the floor away evenly."),
            easierOption = "Use an incline if full floor reps break down."
        ),
        baseExercise(
            name = "Bent-Over Rows",
            sets = 4,
            reps = 10,
            restSeconds = 60,
            summary = "Train upper-back strength and anti-fatigue posture.",
            beginnerSetup = "Hinge at the hips with dumbbells hanging under your shoulders.",
            formCues = listOf("Row toward the lower ribs.", "Keep torso angle steady.", "Lower under control."),
            easierOption = "Use a split stance or one-arm support if your back tires early."
        )
    )

    private fun advancedExercises() = listOf(
        baseExercise(
            name = "Front Squats",
            sets = 4,
            reps = 6,
            restSeconds = 90,
            summary = "Load the legs and trunk with a demanding upright squat pattern.",
            beginnerSetup = "Set the bar or weights in a front-rack position and establish a strong brace.",
            formCues = listOf("Keep elbows high.", "Sit between the hips.", "Drive straight up without folding forward."),
            easierOption = "Use goblet squats if front-rack mobility is the limiter."
        ),
        baseExercise(
            name = "Bulgarian Split Squats",
            sets = 3,
            reps = 8,
            restSeconds = 75,
            summary = "High-value unilateral lower-body strength work.",
            beginnerSetup = "Place the back foot on a bench and find a stance where the front heel stays down.",
            formCues = listOf("Drop straight down.", "Stay stacked over the front leg.", "Drive up through the front foot."),
            easierOption = "Keep the back foot on the floor for a standard split squat."
        ),
        baseExercise(
            name = "Deadlifts",
            sets = 4,
            reps = 5,
            restSeconds = 90,
            summary = "Heavy hinge pattern for total-body strength.",
            beginnerSetup = "Set the bar close to your shins, brace, and pull slack out before lifting.",
            formCues = listOf("Push the floor away.", "Keep the bar close.", "Lock out with glutes, not low-back extension."),
            easierOption = "Use trap-bar or Romanian deadlift variations if needed."
        ),
        baseExercise(
            name = "Dumbbell Bench Press",
            sets = 4,
            reps = 8,
            restSeconds = 75,
            summary = "Build upper-body pressing strength with a stable free-weight pattern.",
            beginnerSetup = "Lie on a bench with feet planted and dumbbells over your chest.",
            formCues = listOf("Lower with control.", "Keep wrists stacked over elbows.", "Press up without bouncing."),
            easierOption = "Use lighter weights or perform on the floor."
        ),
        baseExercise(
            name = "Pull-Ups or Assisted Pull-Ups",
            sets = 4,
            reps = 6,
            restSeconds = 90,
            summary = "Advanced vertical pulling strength work.",
            beginnerSetup = "Start from a dead hang or supported position with shoulders packed down.",
            formCues = listOf("Drive elbows down.", "Keep ribs controlled.", "Lower all the way with control."),
            easierOption = "Use a band or assisted machine to stay in a strong range."
        )
    )

    private fun highlyAdvancedExercises() = listOf(
        baseExercise(
            name = "Barbell Back Squats",
            sets = 5,
            reps = 5,
            restSeconds = 120,
            summary = "Heavy bilateral strength work for experienced lifters.",
            beginnerSetup = "Set the bar securely across the upper back, brace hard, and root your feet before each rep.",
            formCues = listOf("Control the descent.", "Stay braced through the sticking point.", "Drive up with speed while keeping position."),
            easierOption = "Drop to a lighter percentage or use front squats if fatigue is too high."
        ),
        baseExercise(
            name = "Walking Lunges",
            sets = 4,
            reps = 10,
            restSeconds = 75,
            summary = "Demanding unilateral work for strength, control, and work capacity.",
            beginnerSetup = "Stand tall with weights at your sides and step into a long, stable stride.",
            formCues = listOf("Stay tall through the torso.", "Lower under control.", "Push strongly into the next step."),
            easierOption = "Reduce load or perform stationary lunges."
        ),
        baseExercise(
            name = "Barbell Romanian Deadlifts",
            sets = 4,
            reps = 6,
            restSeconds = 90,
            summary = "High-tension hinge work for posterior-chain strength.",
            beginnerSetup = "Hold the bar at lockout, unlock the knees, and brace before hinging.",
            formCues = listOf("Send hips back aggressively.", "Keep lats engaged.", "Stand tall with strong hip extension."),
            easierOption = "Use dumbbells or lower the load if range or grip limits form."
        ),
        baseExercise(
            name = "Deficit Pushups or Ring Pushups",
            sets = 4,
            reps = 10,
            restSeconds = 75,
            summary = "Advanced pressing variation with extra range or instability.",
            beginnerSetup = "Create a stable setup on handles or rings and brace your trunk before each rep.",
            formCues = listOf("Lower under full control.", "Keep shoulders packed.", "Press evenly through both arms."),
            easierOption = "Return to standard pushups if shoulder stability is the limiter."
        ),
        baseExercise(
            name = "Weighted Pull-Ups",
            sets = 5,
            reps = 5,
            restSeconds = 120,
            summary = "High-level pulling strength for trained athletes.",
            beginnerSetup = "Set up with secure loading and a strong dead-hang position.",
            formCues = listOf("Start by pulling shoulders down.", "Drive elbows toward the ribs.", "Control the descent completely."),
            easierOption = "Use bodyweight-only pull-ups if load reduces range quality."
        )
    )

    private fun baseExercise(
        name: String,
        sets: Int,
        reps: Int,
        restSeconds: Int,
        summary: String,
        beginnerSetup: String,
        formCues: List<String>,
        easierOption: String
    ) = ExercisePlan(
        name = name,
        sets = sets,
        reps = reps,
        restSeconds = restSeconds,
        isJointFriendly = false,
        summary = summary,
        beginnerSetup = beginnerSetup,
        formCues = formCues,
        easierOption = easierOption
    )
}
