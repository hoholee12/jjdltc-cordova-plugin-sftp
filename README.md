
SFTP Upload/Download Plugin
===

Cordova plugin to **Download**/**Upload** files from/to a **SFTP** server.

This plugin is build on top of: 
  - [JSch](http://www.jcraft.com/jsch/ "In case you want to be curious") library for **Android**.
  - [NMSSH](http://cocoadocs.org/docsets/NMSSH/2.2.7/index.html "In case you want to be curious") framework for **iOS**.

**Contributors are welcome.**

Platforms supported

* Android
* iOS (Partial)

Installation
---
```
cordova plugin add cordova-sftp-plugin
```
Easy Use  
---  
    
There is a base Object `JJsftp`  expose in `window` that should be initialized with the `host`, `user` and `password` for the server
```javascript
var mySftp = new JJsftp("host", "user", "password");
```
### Methods

* `download(serverPath, localPath [, createIfNotExists, successCallback, errorCallback])` Allow to download a single file.
    * `serverPath` - Path/To/File/In/Server
    * `localPath` - Path/To/File/In/Device
    * `createIfNotExists` - Create the folder path if not exist
    * `successCallback` - Function to call in plugin success
    * `errorCallback` - Function to call in plugin error
* `downloadList(list [, successCallback, errorCallback])` Allow to download a list of files
    * `list` - Object array of files, has the follow attributes:
        * `remote` - Path/To/File/In/Server
        * `local` - Local/Path/To/File
        * [`create`] - default to: `false` - Create the folder path if not exist
    * `successCallback` - Function to call in plugin success
    * `errorCallback` - Function to call in plugin error
* `upload(serverPath, localPath [, successCallback, errorCallback])` Allow to download a single file.
    * `serverPath` - Path/To/File/In/Server
    * `localPath` - Path/To/File/In/Device
    * `successCallback` - Function to call in plugin success
    * `errorCallback` - Function to call in plugin error
* `uploadList(list [, successCallback, errorCallback])` Allow to download a list of files
    * `list` - Object array of files, has the follow attributes 
      * `remote` - Path/To/File/In/Server
      * `local` - Local/Path/To/Fil
    * `successCallback` - Function to call in plugin success
    * `errorCallback` - Function to call in plugin error    
* `cancel([successCallback, errorCallback])` Allow to cancel the asyn process that make the connection
    * `successCallback` - Function to call in plugin success
    * `errorCallback` - Function to call in plugin error

##### Important
Every callback (success or error) will response for the action of add the elements downloads/upload list, if you wanna know when a file or all the files are donwloaded, you should listen to the events fires over document.

### Events  
There are several events that fire during the process, all of it give one arg, the cordova event with few extra attributes  

* `SFTPActionConnected(data)` - Fire on sftp channel connect
    * **data** - object: 
      * `id` - string: UDID related to that connection (**Android Only**) 
* `SFTPActionStart(data)` - Fire when an action is about to start in a file
    * **data** - object:
      * `id` - string: UDID related to that connection (**Android Only**) 
      * `from` - string : Path of the source
      * `to` - string : Path of the target
      * `size` - long : Size of the element
* `SFTPActionProgress(data)` - fire to inform the % of down/up load in the **_actual_** file
    * **data** - object: 
      * `id` - string: UDID related to that connection (**Android Only**) 
      * `percent` - int : % of actual file progress
* `SFTPActionEnd(data)` - fire when the **_actual_** action over a file end
    * **data** - object: 
      * `id` - string: UDID related to that connection (**Android Only**) 
* `SFTPActionListProgress(data)` - Fire after an action (Up/Down) is executed over a file, giving the actual file (index in list) and total files in list
    * **data** - object: 
      * `id` - string: UDID related to that connection (**Android Only**)
      * `progress` - int : Actual element index in list
      * `total` - int : Total count of elements in list
* `SFTPActionListEnd(data)` - Fire at end of action list to inform the # of file reach it
    * **data** - object: 
      * `id` - string: UDID related to that connection (**Android Only**)
      * `all` - boolean : true if all action were made it
* `SFTPActionDisconnected(data)` - Fire on sftp channel disconnect
    * **data** - object: 
      * `id` - string: UDID related to that connection (**Android Only**)
* `SFTPActionCancell(data)` - Fire on cancel async action
    * **data** - object: 
      * `id` - string: UDID related to that connection (**Android Only**)

### Use Example
To Download
```javascript
var sftp        = new JJsftp("host", "user", "password"),
    localPath   = "String/Path/To/Place/The/Download"
    filelist    = [{
          remote    : "/Path/To/Remote/File.*"
        , local     : localPath+"file.*"
    },{
          remote    : "/Path/To/Another/Remote/File.*"
        , local     : localPath+"anotherFile.*"
    }];

    sftp.downloadList(filelist, function(data){
        /* Wow everything goes good, but just in case verify data.success */
    }, function(error){
        /* Wow something goes wrong, check the error.message */       
    });
```
To Upload is the same but calling `upload` or `uploadList` and by default will override a file if exists in server

Of course if you want, you could add any of the JJsftp events to document and listen the progress of the download by example

#### Know Issues
- `upload` does not work on **iOS** (Yet)
- `cancel` does not work on **iOS** (Yet), therefore `SFTPActionCancell` is never trigger.
