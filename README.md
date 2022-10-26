bladenightapp-server
====================

The server side of the Bladenight application.


This software is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this software.  If not, see <http://www.gnu.org/licenses/>.

Deploy App
    in Terminal run
        gradle clean
        gradle jar
        gradle shadowJar 
    this will create runnable file in build/libs/bladenightapp-server-0.6-SNAPSHOT-all.jar
    create PKCS12 file .p12 with create-certificate.sh

issues
    Execution failed for task ':shadowJar'. 
        > Unsupported class file major version 60
    move through all steps for deploy app. Important is 'gradle clean' on startup


Ideas for Applicationupdates
    Load Images and links from Server
    check gaps for police in train
    don't allow long time differences on train length/ from to head and train - head etas
    calculate time by trainlength
    seems using wrong speeddatas to update etas
    renew wamp to newer version

