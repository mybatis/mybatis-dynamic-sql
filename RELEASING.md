# Release Information

The project is released with the normal Maven release cycle except for the site.

## Pre-Requisites

1. Get authorization to Sonatype and make sure your GPG key is setup and registered per instructions here:
   https://github.com/mybatis/committers-stuff/wiki/Release-Process
2. Make sure your SSH key is setup at GitHub

## Release Process

1. Clone the main repo (with ssh), checkout the master branch
2. mvn release:prepare
3. mvn release:perform
4. Logon to https://oss.sonatype.org/
5. Find the mybatis staging repo
6. Verify everything looks OK
7. Close the repo
8. Release the repo

## Update the Site

Automatic site deployment plugin is broken on Mac M1 and with newer keys on any platform. Therefore, it is disabled
in the normal release. Here's how to do it manually:

1. Clone the main repo and checkout the release tag:
   - `git clone git@github.com:mybatis/mybatis-dynamic-sql.git`
   - `cd mybatis-dynamic-sql`
   - `git checkout mybatis-dynamic-sql-1.5.0`
2. `./mvnw clean site`
3. Checkout a copy of the main repo in a temp directory:
   - `mkdir ~/temp/temp-mybatis`
   - `cd ~/temp/temp-mybatis`
   - `git clone git@github.com:mybatis/mybatis-dynamic-sql.git`
   - `git checkout gh-pages`
4. Copy the generated site into the temp checkout:
   - `cp -R <<source git>>/mybatis-dynamic-sql/target/site ~/temp/temp-mybatis/mybatis-dynamic-sql`
5. Push the new site:
   - `cd ~/temp/temp-mybatis/mybatis-dynamic-sql`
   - `git add .`
   - `git commit -m "Manual Site Update 1.5.0"`
   - `git push`
6. Delete the temporary checkout
   - `rm -R ~/temp/temp-mybatis`
