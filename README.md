AndroidOnFire
=============

A library to use [Firebase][firebase] on a native Android application.

# Installation

* Pull the files from the repository
* Setup an Android library project
* Add the project as a dependency of you own project

# Usage

_AndroidOnFire_ is designed to mirror exactly what you would do when using the Javascript library.
The only main difference is in the initialization. 

You have to create a `FirebaseEngine` object that you will use to get a reference to the `Firebase` object.

## FirebaseLoaded listener

You can only create `Firebase` objects when the engine has been loaded.

## Do not change activities !

In a web application using Firebase you would not refresh the page because you don't want to lose the current state. 
This is also true for _AndroidOnFire_. You should only use one Activity while using Firebase. This may seem a bit counter-intuitive and does not follow Android's guidelines. I agree on that. This is sadly the only way for now to get a responsive app, since it takes a few seconds to load the engine.

Fortunately the Android team give us the ability to use Fragments. You can mimic the change of screen be embedding Fragment in your sole Activity and use the backstack to handle the back buttons.

## Using ProGuard

Since we use a WebView to communicate between the Firebase Javascript and the native app, we have to make sur that the calls from the Javascript are correctly routed.

If you are using ProGuard, add these lines to your configuration file:

    -keepclassmembers class pro.schmid.android.androidonfire.FirebaseJavaScriptInterface {
       public *;
    }
    
# Example app

You can see the example app [FireDrawer][2] which allows your to draw on a canvas with multiple users.

Another example app is *Where Are You*. It has both an [Android version][3] ([source][4]) and a [web version][5] ([source][6]).

# How does it work?

Internally, `FirebaseEngine` creates an invisible [WebView][1] in the current Activity. It then execute the Firebase.js Javascript and passes events between the WebView and the engine.

For this reason, you cannot have multiple activities since the WebView might be destroyed.

[1]: https://developer.android.com/reference/android/webkit/WebView.html
[2]: https://github.com/jschmid/FireDrawer
[firebase]: https://www.firebase.com/
[3]: https://play.google.com/store/apps/details?id=pro.schmid.android.whereareyou
[4]: https://github.com/jschmid/WhereAreYou-Android
[5]: http://w.schmid.pro/
[6]: https://github.com/jschmid/WhereAreYou-Web