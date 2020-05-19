# rd-judicial-data-load
Judicial reference data load (JRD)

JRD is batch application and JRD batch is scheduled with kuberenetes which runs once in day per cluster.

JRD consume data files from an external source, transform that data into the destination format 
and load the data into JRD database. 

#Consumption of files from a SFTP server
The files received from SFTP server are encrypted using GPG encryption (which complies with OpenPGP standards).

An internal SFTP server (behind a F5 Load balancer) will poll the files at periodic intervals from the  external SFTP server. It will forward the files onto the untrusted network that Palo Alto is listening on.

The Palo Alto untrusted interfaces will form the Palo Alto backend pool, used by requests matched by the path-based rule.

The files are  decrypted and then scanned and if everything is okay then trusted traffic is sent to a configured endpoint, in this case an Azure Blob Storage account.


#Data Transformation and Load - This is achieved through a K8S scheduler and Apache Camel.
Kubernetes scheduler triggers Apache Camel routes which process files stored in Azure blob storage and persists it JRD database.

# Building and deploying the application
Building the application
The project uses Gradle as a build tool. It already contains ./gradlew wrapper script, so there's no need to install gradle.

To build the project execute the following command:

  ./gradlew build 
