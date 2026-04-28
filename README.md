# Online Sabzi Shop - Android App

A production-style vegetable shopping app built with Kotlin, Jetpack Compose, Clean Architecture, MVVM, Hilt, and Firebase.

---

## 🚀 Setup Instructions

### 1. Firebase Project Setup

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new project: **SabziShop**
3. Register your Android app with package name: `com.azhar.sabzishop`
4. Download **`google-services.json`** and place it in: `app/google-services.json`
5. Enable **Firebase Authentication** → Email/Password sign-in method
6. Enable **Cloud Firestore** → Start in **test mode** (or use the security rules below)

### 2. Gradle Sync

After placing `google-services.json`, sync the project:
- In Android Studio: **File → Sync Project with Gradle Files**

### 3. Admin Account Setup

After first signup, you need to set the `role` field to `"admin"` manually in Firestore:

1. Open Firebase Console → Firestore Database
2. Navigate to: `users/{your-uid}`
3. Change the `role` field from `"customer"` to `"admin"`
4. Restart the app and log in — you'll be routed to the Admin Dashboard

---

## 🔒 Firestore Security Rules

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Users - read/write own document
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    // Cart - read/write own cart
    match /users/{userId}/cart/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    // Products - anyone can read, only admins can write
    match /products/{productId} {
      allow read: if true;
      allow write: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
    }

    // Orders - users can create/read own, admins can read/update all
    match /orders/{orderId} {
      allow create: if request.auth != null;
      allow read: if request.auth != null && (
        resource.data.userId == request.auth.uid ||
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin"
      );
      allow update: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
    }
  }
}
```

---

## 📦 Project Architecture

```
com.azhar.sabzishop
├── SabziShopApp.kt              # @HiltAndroidApp
├── MainActivity.kt              # @AndroidEntryPoint, hosts NavGraph
├── di/
│   ├── AppModule.kt             # Firebase singletons
│   └── RepositoryModule.kt      # Interface → Impl bindings
├── domain/
│   ├── model/                   # Pure Kotlin data classes
│   ├── repository/              # Repository interfaces
│   └── usecase/                 # Business logic use cases
├── data/
│   ├── model/                   # Firestore DTOs
│   ├── mapper/                  # DTO ↔ Domain mappers
│   ├── datasource/              # Raw Firebase calls
│   └── repository/              # Repository implementations
├── presentation/
│   ├── navigation/              # NavGraph + Screen routes
│   ├── components/              # Reusable composables
│   ├── auth/                    # Splash, Welcome, Login, SignUp, ForgotPassword
│   ├── user/                    # Home, ProductDetails, Cart, Checkout, Orders, Profile
│   └── admin/                   # Dashboard, ManageProducts, Add/Edit Product, Orders
└── utils/
    ├── Constants.kt
    ├── Resource.kt              # Loading/Success/Error wrapper
    └── ImageUtils.kt            # Base64 ↔ Bitmap conversion
```

---

## 🖼 Base64 Image Flow

**Admin uploads image:**
1. Gallery picker → `Uri`
2. `ImageUtils.uriToBase64(context, uri)` → compresses to 300×300 JPEG @ 60% quality
3. Base64 string saved inside Firestore product document

**User views image:**
1. `ImageUtils.base64ToBitmap(base64)` → `Bitmap`
2. `bitmap.asImageBitmap()` → displayed in Compose `Image` composable

> ⚠️ Firestore documents have a 1 MB size limit. Images are compressed before encoding to stay well within limits.

---

## 🏗 Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | Clean Architecture + MVVM |
| State | StateFlow + collectAsStateWithLifecycle |
| DI | Hilt |
| Navigation | Navigation Compose |
| Auth | Firebase Authentication (Email/Password) |
| Database | Cloud Firestore |
| Images | Base64 encoded in Firestore |
| Async | Coroutines + Flow |

---

## 👥 Roles

| Role | Access |
|---|---|
| **Guest** | Browse products, view details |
| **Customer** | All guest features + cart, orders, profile |
| **Admin** | Admin dashboard, manage products, manage orders |

Role is stored in `users/{uid}.role` in Firestore (`"customer"` or `"admin"`).

