# Overview
This document will provide a detailed analysis into the functionality of this software and will define what success for the software looks like. This document will serve as a blueprint for the construction of the software, illustrating what the software must be capable of

# Software Requirements
This section provides a set of functional and nonfunctional requirements that the software must follow

## Functional Requirements
| ID | Requirement |
| :-------------: | :----------: |
| FR1 | The system must connect to Twitch’s APIs using client credentials |
| FR2 | The system must connect to StreamerSonglist’s API to retrieve the most recently played song |
| FR3 | The system must record the start timestamp of each song that the streamer plays |
| FR4 | The system must create a text file when the stream ends containing timestamped song data |
| FR5 | The system must be able to notify the user when credentials are invalid in addition to which credentials are invalid |
| FR6 | The system must store the ongoing song list locally until the stream ends |
| FR7 | The system must allow users to change the output folder path for the user |
| FR8 | The system must automatically name the text file based on the date of the stream |
| FR9 | The system must gracefully handle error retries and API rate limiting |
| FR10 | The system must log errors/warnings for debugging |

## Non-Functional Requirements
| ID | Requirement |
| :-------------: | :----------: |
| NFR1 | The system must be able to recover from temporary API outages without data loss |
| NFR2 | The system must be able to recover from a system crash without data loss |
| NFR3 | The system must be able to handle being started mid-stream, up to 15 minutes after the stream begins and still capture past songs |
| NFR4 | The system must maintain consistent performance and behavior (with up to 1% difference in average and peak performance across consistent hardware) across Windows, MacOS, and Linux |
| NFR5 | The system must store API credentials securely in both environment variables and encrypted files |
| NFR6 | The system must generate the output songlist file within 5 seconds of the stream ending |
| NFR7 | The system must comply with all of Twitch's and StreamerSonglist Terms of Service |

# Change management plan
To accomodate for changes within the software, a well defined process will be followed. Any customer, stakeholder, or user that wishes for a change within the software can go through the process of completing a change order. A formal writeup of the change request will be documented in a Change Log for future reference with details such as status, approval date, and implementation version being included. When a change request is made, the software will be developed on a isolated development Git Branch to allow for changes to be commited, reviewed, and then merged into the production branch. This will allow for changes to be inspected and approved by others before being deployed into production.

# Traceability links
This section associates each of the Artifact Names found within all of the artifacts with a set of requirement IDs

## Class Diagram Traceability
| Artifact Name | Requirement ID |
| :-------------: |:----------: |
| TwitchAPI | FR1, FR5, FR9, FR10, NFR1, NFR2, NFR3, NFR4, NFR6, NFR7 |
| StreamerSonglistAPI | FR2, FR3, FR4, FR6, FR9, FR10, NFR1, NFR2, NFR3, NFR4, NFR7 |
| File Writer | FR4, FR7, FR8, FR10, NFR4, NFR5, NFR6, NFR7 |
| Web Client | FR5, FR10, NFR1, NFR2, NFR3, NFR4 NFR5, NFR7 |
| Token Manager | FR5, FR10, NFR1, NFR2, NFR3, NFR4, NFR5, NFR7 |
| Client Info | FR1, FR5, NFR4, NFR5, NFR7 |

# Software Artifacts
Below are each of the aforementioned artifacts with embedded links to view them
* [Class Diagram](https://github.com/StevenStreasick/CIS-611-Song-Recorder/blob/master/artifacts/Class%20Diagram.png)
* [Use Case Diagram](https://github.com/StevenStreasick/CIS-611-Song-Recorder/blob/master/artifacts/Sequence%20Diagram.png)
