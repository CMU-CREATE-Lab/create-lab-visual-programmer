Creating a Keystore
-------------------
Here's what I did to create the keystore so we can self-sign the jars.  Do the following in the Terminal, with the
current directory set to this directory.  When prompted, I used the password "password".  The last step, "keytool -list"
is optional, and is just used to verify that everything worked OK.

$ keytool -genkey -keystore CREATELabKeystore -alias createlab
  Enter keystore password:
  Re-enter new password:
  What is your first and last name?
    [Unknown]:  CREATE Lab
  What is the name of your organizational unit?
    [Unknown]:  Robotics Institute
  What is the name of your organization?
    [Unknown]:  Carnegie Mellon University
  What is the name of your City or Locality?
    [Unknown]:  Pittsburgh
  What is the name of your State or Province?
    [Unknown]:  Pennsylvania
  What is the two-letter country code for this unit?
    [Unknown]:  US
  Is CN=CREATE Lab, OU=Robotics Institute, O=Carnegie Mellon University, L=Pittsburgh, ST=Pennsylvania, C=US correct?
    [no]:  yes

  Enter key password for <createlab>
  	(RETURN if same as keystore password):

$ keytool -selfcert -alias createlab -keystore CREATELabKeystore
  Enter keystore password:

$ keytool -list -keystore CREATELabKeystore
  Enter keystore password:

  Keystore type: JKS
  Keystore provider: SUN

  Your keystore contains 1 entry

  createlab, Dec 21, 2011, PrivateKeyEntry,
  Certificate fingerprint (MD5): B4:96:B2:C3:9F:76:8B:DE:6B:BC:74:93:1F:9C:8A:75
