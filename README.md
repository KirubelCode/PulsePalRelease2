PulsePal – Real-Time Fitness & Heart Rate Monitoring App
PulsePal is a full-stack Android fitness application that integrates with Movesense ECG sensors to track real-time heart rate, movement, and workout metrics. 
Built for athletes, fitness enthusiasts, and health-conscious users, the app provides live feedback and post-workout analytics powered by real sensor data — 
demonstrating strong mobile, IoT, and backend development skills.

🌐 Download the APK: pulsepal.store

Key Features
- Real-Time Heart Monitoring: Live ECG-based heart rate tracking with <500ms delay using Movesense sensors.

- Step & Motion Tracking: Calculates steps, distance, and pace from accelerometer data.

- Calorie Burn Estimation: Uses heart rate and motion to estimate calories burned based on personal data.

- Workout Summary Reports: Displays post-session analytics including average/max HR, steps, calories, and more.

- User Authentication: Secure login/signup and cloud-sync for personal workout history.

- Clean, Intuitive UI: Mobile-optimized interface with real-time graphs and colored HR zones.

----

Technologies Used
- Android (Java) – Mobile client with sensor UI and activity flow.

- Movesense SDK – Bluetooth ECG sensor integration.

- Node.js & Express – RESTful API for authentication and data storage.

- MySQL – Stores user profiles and workout logs.

- Gradle, JSON, Jetpack – Standard Android development tools.

- Hosted Web APK – Deployed at pulsepal.store for easy access.

-----

Repository Overview
/src/ – Android app source code and layouts

/Hosted-PHP-MYSQL/ – Backend scripts and SQL database

/release/ – APK and deployment assets

/Project Report/ – Full project documentation (PDF)

------

Why I Built This
I’ve always been passionate about fitness and tech. I wanted to build something beyond step counters — something using real, accurate physiological data. 
PulsePal bridges IoT and mobile development by turning real ECG signals into actionable insights, all wrapped in a user-friendly app. 
This project helped me grow as a full-stack developer and showed me what’s possible when software meets real-world sensors.

What Makes PulsePal Different - Future Changes and Enhancements
Very-Fast Sensor Data – Retrieves real-time sensor data (especially linear acceleration) with <500ms latency, 
thanks to direct integration with Movesense and efficient implementation using Android Jetpack. 
Built natively for Android in Java, it ensures low-latency processing — a key advantage over many cross-platform or slower fitness apps, 
and a strong foundation for future real-time features like motion alerts or form correction.

Coming Soon / Ideas to Expand:
- Heart rate zone alerts (e.g. visual/vibration feedback).
- GPS-based distance and route tracking.
- Offline sessions for less reiability on an internet connection.
- Add a rewarding component to the app such as badges for milestones reached.

