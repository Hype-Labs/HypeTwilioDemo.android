
![alt tag](https://hypelabs.io/static/img/NQMAnSZ.jpg)

![alt tag](https://hypelabs.io/static/img/logo200x.png)

[Hype](https://hypelabs.io/?r=10) is an SDK for cross-platform peer-to-peer communication with mesh networking. Hype works even without Internet access, connecting devices via other communication channels such as Bluetooth, Wi-Fi direct, and Infrastructural Wi-Fi.

The Hype SDK has been designed by [Hype Labs](http://hypelabs.io/?r=10). It is currently available for multiple platforms.

You can [start using Hype](http://hypelabs.io/?r=10) today.

## What does it do?

This project consists of a chat app sketch written to illustrate an integration between two technology's, Hype framework and Twilio Android SDK.
This demo allows users to communicate with a public chat room even if they don't have internet access.

This demo does not serve the purpose of documenting how to use the SDK. In case you're looking for documentation, check our quickstart guides and project templates on the [apps](https://hypelabs.io/apps/?r=10) page, or our [documentation](https://hypelabs.io/docs/?r=10) page.

## Setup

This demo does not work out of the box. The following are the necessary steps to configure it:

 1. [Download](https://hypelabs.io/downloads/?r=10) the SDK binary for Android.
 2. Extract it, and drag it to the project root folder
 3. Access the [apps](https://hypelabs.io/apps/?r=10) page and create a new app
 4. Name the app and press "Create new app"
 5. Go to the app settings
 6. Copy the identifier under `App ID`
 7. With the project open on Android Studio, in the file `HypeController.java`, find the line that reads `Hype.setAppIdentifier("{{app_identifier}}");`
 8. Replace `{{app_identifier}}` by the App ID you just copied
 9. Go back to the app settings
 10. This time, copy the `Access Token`, under the Authorization group
 11. Open the same file, `ChatApplication.java`, and find the method declaration for `public String onHypeRequestAccessToken`
 12. Where that method returns `{{access_token}}`, have it return the token you just copied instead

You should be ready to go! If you run into trouble, feel free to reach out to our [community](https://hypelabs.io/community/?r=10) or browse our other [support](https://hypelabs.io/support/?r=10) options. Also keep in mind our project templates on the [apps](https://hypelabs.io/apps/?r=10) page for demos working out of the box.

## Setup Twilio SDK

The first thing we need to do is grab all the necessary configuration values from our Twilio account.

## Gather Account Information

The first thing we need to do is grab all the necessary configuration values from our
Twilio account. To set up our back-end for Chat, we will need five 
pieces of information:

| Config Value  | Description |
| :-------------  |:------------- |
Service Instance SID | Like a database for your Chat data - [generate one in the console here](https://www.twilio.com/console/chat/services)
Account SID | Your primary Twilio account identifier - find this [in the console here](https://www.twilio.com/console/chat/getting-started).
API Key | Used to authenticate - [generate one here](https://www.twilio.com/console/chat/dev-tools/api-keys).
API Secret | Used to authenticate - [just like the above, you'll get one here](https://www.twilio.com/console/chat/dev-tools/api-keys).
Mobile Push Credential SID | Used to send notifications from Chat to your app - [create one in the console here](https://www.twilio.com/console/chat/credentials) or learn more about [Chat Push Notifications in Android](https://www.twilio.com/docs/api/chat/guides/push-notifications-android).

## Create a Twilio Function

When you build your application with Twilio Chat, you will need two pieces - the client (this Android app) and a server that returns access tokens. If you don't want to set up your
own server, you can use [Twilio Functions](https://www.twilio.com/docs/api/runtime/functions) to easily create this part of your solution. 

If you haven't used Twilio Functions before, it's pretty easy - Functions are a way to 
run your Node.js code in Twilio's environment. You can create new functions on the Twilio Console's [Manage Functions Page](https://www.twilio.com/console/runtime/functions/manage).

You will need to choose the "Programmable Chat Access Token" template, and then fill in the account information you gathered above. After you do that, the Function will appear, and you can read through it. Save it, and it will immediately be published at the URL provided - go ahead and put that URL into a web browser, and you should see a token being returned from your Function. If you are getting an error, check to make sure that all of your account information is properly defined.

Want to learn more about the code in the Function template, or want to write your own server code? Checkout the [Twilio Chat Identity Guide](https://www.twilio.com/docs/api/chat/guides/identity) for the underlying concepts.

Now that the Twilio Function is set up, let's get the starter Android app up and running.

## Configure and Run the Mobile App

At the time of writing, the Twilio Chat library does not run in x86-64 emulators.
You may need to run the Android app on your own phone or tablet.

Once the app loads, you should see a UI like this one:

![quick start app screenshot](http://i.imgur.com/WgKdQcr.png)

Start sending yourself a few messages - they should start appearing both in a
`RecyclerView` in the starter app, and in your browser as well if you kept that
window open.

You're all set! From here, you can start building your own application. For guidance
on integrating the Android SDK into your existing project, [head over to our install guide](https://www.twilio.com/docs/api/chat/sdks).
If you'd like to learn more about how Chat works, you might want to dive
into our [user identity guide](https://www.twilio.com/docs/api/chat/guides/identity), 
which talks about the relationship between the mobile app and the server.

## License

This project is MIT-licensed.