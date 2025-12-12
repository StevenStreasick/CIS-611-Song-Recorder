# Overview
This document will provide a detailed analysis into the functionality of this software and will define what success for the software looks like. This document will serve as a blueprint for the construction of the software, illustrating what the software must be capable of

# Software Requirements
This section provides a set of functional and nonfunctional requirements that the software must follow

## Functional Requirements
| ID | Requirement |
| :-------------: | :----------: |
| FR1 | The system must subscribe to Twitch's Streamer Online and Offline webhook events for the configured streamer |
| FR2 | The system must correctly handle Twitch webhook verification challenges by returning the required challenge response |
| FR3 | The system must prompt the user for required Twitch credentials |
| FR4 | The system must detect invalid Twitch Credentials and notify the user when found |
| FR5 | The system must connect to the StreamerSonglist’s API to retrieve the most recently played song for the configured streamer |
| FR6 | The system must record the end timestamp of each song that the streamer plays |
| FR7 | The system must maintain the songlist in memory for the duration of the stream |
| FR8 | The system must create a text file containing the list of songs played along with the time the streamer starts playing when the stream ends (Twitch Offline event detected) |
| FR9 | The system must allow users to set and change the output folder path for the user before recording begins |
| FR10 | The system must automatically name the output text file based on the stream date (e.g., streamername-MM:DD:YYYY.txt) unless overriden |
| FR11 | The system must log errors and warnings to console with information regarding timestamps, error message, and stack trace for debugging |
| FR12 | The system must detect and ignore duplicate song entries found |

## Non-Functional Requirements
| ID | Requirement |
| :-------------: | :----------: |
| NFR1 | The system must be able to recover from temporary API outages without data loss and automatically reconnect |
| NFR2 | The system must be able to maintain the current song list in memory during temporary disconnects |
| NFR3 | The system must be able to recover from temporary disconnects by adding missed songs to the song list |
| NFR4 | The system must be able to handle being started mid-stream, up to 15 minutes after the stream begins and still capture past songs |
| NFR5 | The system must maintain consistent performance and behavior (with up to 1% difference in average and peak performance across consistent hardware) across Windows, MacOS, and Linux |
| NFR6 | The system must store API credentials securely in both environment variables and encrypted files |
| NFR7 | The system must generate the output songlist file within 5 seconds of the stream ending in normal conditions |
| NFR8 | The system must comply with all of Twitch's and StreamerSonglist Terms of Service | 
| NFR9 | The system must be able to automatically refresh authentication tokens when invalid |
| NFR10 | The system must be able to default to a OAuth flow when either no authentication tokens are found or are unable to be refreshed |
| NFR11 | The system must provide structured logging such as socket connects/disconnects, subscriptions, and retry attempts to the console |

# Change management plan
To accomodate for changes within the software, a well defined process will be followed. Any customer, stakeholder, or user that wishes for a change within the software can go through the process of completing a change order. A formal writeup of the change request will be documented in a Change Log for future reference with details such as status, approval date, and implementation version being included. When a change request is made, the software will be developed on a isolated development Git Branch to allow for changes to be commited, reviewed, and then merged into the production branch. This will allow for changes to be inspected and approved by others before being deployed into production.

# Traceability links
This section associates each of the Artifact Names found within all of the artifacts with a set of requirement IDs

## Traceability Matrix

Activity Diagram
| Requirement ID | Use Case Diagram | Class Diagram | Sequence Diagram | Activity Diagram |
| :-------------: |:----------: |:----------: |:----------: |:----------: |
| FR1 | Subscribe To Events | Main, TwitchAPI | Subscribe To Streamer Events | Subscribe To Twitch Events |
| FR2 | Subscribe To Events | TwitchAPI | Subscribe To Streamer Events | Subscribe To Twitch Events |
| FR3 | Login | Main, TwitchAPI, Token Manager | Prompted Login Information | Ask Users For Credentials |
| FR4 | Authenticate With Twitch | Main, TwitchAPI, Token Manager, Web Client | Validate Stored Login Information, Prompted Login Information | Validate Credentials |
| FR5 | Song Changed | StreamerSonglistAPI | Listen For Song List Updates | Listen For Song History Change |
| FR6 | Song Changed | StreamerSonglistAPI | Write Songlist, Write to File | Timestamp Song |
| FR7 | Store Song/Timestamp | StreamerSonglistAPI | Listen For Song List Updates | Timestamp Song |
| FR8 | Store Song/Timestamp | StreamerSonglistAPI, File Writer | Write To File | Write Songlist To File |
| FR9 | Init | Main, StreamerSonglistAPI, File Writer | Subscribe To Streamer Events | Subscribe To Twitch Events |
| FR10 | Store Song/Timestamp | StreamerSonglistAPI, File Writer | Write Songlist, Write To File | Write Songlist To File |
| FR11 | Subscribe To Events | TwitchAPI | Subscribe To Streamer Events | Subscribe To Twitch Events |
| FR12 | Store Song/Timestamp | StreamerSonglistAPI | Listen For SOng List Updates | Listen For Song History Change |
| NFR1 | Subscribe To Events | TwitchAPI, StreamerSonglistAPI, Web Client, CallbackServer | ALL | ALL |
| NFR2 | Store Song/Timestamp | StreamerSonglistAPI | Listen For Sogn List Updates | Timestamp Song |
| NFR3 | Song Changed | StreamerSonglistAPI | Listen For Song List Updates | Listen For Song History Change |
| NFR4 | Login, Authenticate With Twitch | TwitchAPI, StreamerSonglistAPI | Listen For Song List Updates | Listen For Song History Change |
| NFR5 | ALL | ALL | ALL | ALL |
| NFR6 | Get Twitch Tokens | TwitchAPI, Token Manager, File Writer | Store Validated ClientID/ClientSecret, Write Token Information | Store Auth Token |
| NFR7 | Store Song/Timestamp | TwitchAPI, StreamerSonglistAPI, File Writer | Write Song List, Write To File | Wait For Streamer Offline, Write Songlist To File |
| NFR8 | ALL | ALL | ALL | ALL |
| NFR9 | Store Song/Timestamp | Token Manager, Web Client, CallbackServer | Get Twitch OATH Token | Get Auth Token |
| NFR10 | Get Twitch Tokens | Web Client, CallbackServer | User Authenticate | Authorize App |
| NFR11 | Subscribe To Events, Song Changed | TwitchAPI, StreamerSonglistAPI, Web Client | Subscribe To Streamer Events | Subscribe To Twitch Events |

# Software Artifacts
Below are each of the aforementioned artifacts with embedded links to view them
* [Class Diagram](https://github.com/StevenStreasick/CIS-611-Song-Recorder/blob/master/artifacts/Class%20Diagram.png)
* [Use Case Diagram](https://github.com/StevenStreasick/CIS-611-Song-Recorder/blob/master/artifacts/Sequence%20Diagram.png)
