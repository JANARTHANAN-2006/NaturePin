[README.md](https://github.com/user-attachments/files/27862148/README.md)
# 🌿 NaturePin

<div align="center">

# 🗺️ Discover Hidden Nature Spots Around You

### *A modern geospatial Android platform built for explorers, hikers, photographers, and eco-travelers.*

<img src="https://img.shields.io/badge/Platform-Android-brightgreen?style=for-the-badge&logo=android" />
<img src="https://img.shields.io/badge/Language-Kotlin-blue?style=for-the-badge&logo=kotlin" />
<img src="https://img.shields.io/badge/Backend-Supabase-3FCF8E?style=for-the-badge&logo=supabase" />
<img src="https://img.shields.io/badge/Database-PostgreSQL-blue?style=for-the-badge&logo=postgresql" />
<img src="https://img.shields.io/badge/Maps-MapLibre-orange?style=for-the-badge" />

<br>
<br>

> **NaturePin helps users discover, preserve, and share hidden natural locations through an intelligent community-driven mapping system.**

</div>

---

# 📖 About The Project

NaturePin is a startup-style Android application designed to bridge the gap between modern digital mapping and nature exploration.

Unlike traditional map platforms overloaded with commercial listings and advertisements, NaturePin focuses entirely on:

- 🌱 Nature discovery
- 📍 Hidden location sharing
- 🧭 Community exploration
- 🌍 Geographic preservation

The platform combines real-time cloud synchronization with offline-first local persistence to create a fast, scalable, and secure exploration experience.

---

# ✨ Features

## 🗺️ Interactive Geospatial Mapping
- Real-time vector map rendering using **MapLibre GL**
- Smooth hardware-accelerated navigation
- Dynamic location pinning
- Nature-focused exploration interface

---

## ☁️ Hybrid Cloud + Offline Architecture

NaturePin uses a dual-persistence architecture for both speed and reliability.

| System | Purpose |
|---|---|
| **Supabase + PostgreSQL** | Global sync, authentication, shared pins |
| **Room Database** | Offline storage, instant local access |

### Benefits
✅ Faster loading  
✅ Offline support  
✅ Reliable synchronization  
✅ Better scalability  

---

## 🔒 Secure Authentication
- Supabase Authentication
- Secure session persistence
- Encrypted API communication
- Row-Level Security (RLS)

---

## ⚡ High Performance Experience
- Optimized image loading with Glide
- Efficient caching system
- Low-latency database access
- Smooth map rendering

---

# 🧠 System Architecture

```text
Android Application
        │
        ▼
 Retrofit / Ktor Networking
        │
        ▼
 Supabase Backend Services
        │
        ▼
 PostgreSQL Database
```

---

# 🛠️ Tech Stack

## 📱 Frontend
- Kotlin
- Android SDK
- XML Layouts
- Material Design

## 🗺️ Maps
- MapLibre GL

## ☁️ Backend
- Supabase
- PostgreSQL

## 🗄️ Local Database
- Room Database
- SharedPreferences

## 🌐 Networking
- Retrofit
- Ktor

## 🖼️ Media Handling
- Glide

---

# 📸 Screenshots

> Add your screenshots inside:
>
> `assets/screenshots/`

| Home Screen | Map View | Add Pin |
|---|---|---|
| ![](assets/screenshots/home.png) | ![](assets/screenshots/map.png) | ![](assets/screenshots/pin.png) |

---

# 🚀 Installation

## Prerequisites

- Android Studio Ladybug or newer
- Android SDK API 24+
- Supabase Project
- Internet Connection

---

## Clone Repository

```bash
git clone https://github.com/YOUR_USERNAME/NaturePin.git
```

---

## Configure Supabase

Create a `local.properties` file and add:

```properties
SUPABASE_URL=YOUR_SUPABASE_URL
SUPABASE_KEY=YOUR_SUPABASE_KEY
```

---

## Run The Project

```bash
./gradlew assembleDebug
```

Or simply click ▶ **Run** in Android Studio.

---

# 📂 Project Structure

```text
NaturePin/
│
├── app/
│   ├── ui/
│   ├── auth/
│   ├── maps/
│   ├── database/
│   ├── network/
│   └── repository/
│
├── gradle/
├── assets/
└── README.md
```

---

# 🌍 Why NaturePin?

NaturePin addresses a real-world problem:

> Many beautiful natural locations remain undocumented or hidden beneath commercial map clutter.

NaturePin creates a focused ecosystem for:
- Environmental discovery
- Geographic preservation
- Community exploration
- Outdoor adventure sharing

---

# 📈 Scalability

NaturePin is built using scalable startup-oriented architecture principles.

### Designed For:
- Community growth
- Cloud scalability
- Feature modularity
- Backend extensibility

### Future Expansion
- AI-based recommendations
- Offline downloadable maps
- Community moderation
- Environmental analytics
- Social exploration feeds

---

# 🔐 Security

NaturePin follows secure engineering practices:

✅ Secure authentication  
✅ Encrypted API communication  
✅ Android sandboxed storage  
✅ Session persistence  
✅ Backend row-level security  

---

# 🤝 Contributing

Contributions are welcome.

```bash
Fork the repository
Create your feature branch
Commit your changes
Open a pull request
```

---

# 📄 License

This project is licensed under the MIT License.

---

# 👨‍💻 Developer

Built with passion for:
- Android Development
- Geospatial Technology
- Community Mapping
- Environmental Exploration

---

<div align="center">

# ⭐ If you like this project, consider starring the repository!

### 🌿 “Maps should help people discover nature — not advertisements.”

</div>
