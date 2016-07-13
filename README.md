# Comfort-Vote

[![Join the chat at https://gitter.im/rafaie/comfort-vote](https://badges.gitter.im/rafaie/comfort-vote.svg)](https://gitter.im/rafaie/comfort-vote?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

**It's Android application to collect comfort votes. This application can connect Microsoft Smart band 2 and collect the sensor data in different method.** 

Now, It covers three different sampling data method:
* **Direct Comfort vote:** By this functionality, the vote and a sample data is stored. As well, it's would be scheduled in setting form to raise a notification and ask to user to vote.
* **Automatic Sampling data:** It helps to automatically connect to device and get a sample data and stored in the Mobile phone database. Now, the previous vote, humidity and clothed info replicated to new sample data. This section is designed to works as a separate thread and it auto-start and works as a background service. In the setting form, this functionality can be customized completely. 
* **Continuous sampling:** It's used for continues activity test such as typing test. In this functionality, sampling data is on higher possible rate (SampleRate.MS128)

