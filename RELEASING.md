# Release Information

The project is released with the normal Maven release support.

## Pre-Requisites

1. Get authorization to Sonatype and make sure your GPG key is setup and registered per instructions here:
   https://github.com/mybatis/committers-stuff/wiki/Release-Process
2. Make sure your SSH key is setup at GitHub
3. Create a Maven Central Publishing token if you don't have one, or it is expired:
   - Logon to https://central.sonatype.com/
   - Go to Profile, View User Tokens
   - Generate a new token if you need to and add it to the maven settings file
4. Make sure your Maven `settings.xml` file is correct. At a minimum, it should contain the following:
    ```xml
    <settings>
      <servers>
        <server>
          <id>gh-pages-scm</id>
          <configuration>
            <scmVersionType>branch</scmVersionType>
            <scmVersion>gh-pages</scmVersion>
          </configuration>
        </server>
        <server>
          <id>central</id>
          <username>[tokenized user name]</username>
          <password>[tokenized password]</password>
        </server>
      </servers>
    </settings>
    ```

## Preparation

1. Update the release date in the CHANGELOG

## Release Process

1. Clone the main repo (with ssh), checkout the master branch
2. If you are on a Unix or Mac, then setup GPG to use the terminal when asking for your password: `export GPG_TTY=$(tty)`
3. `./mvnw release:prepare`
4. `./mvnw release:perform`
5. Logon to https://central.sonatype.com/
6. Go to Profile->View Deployments
7. Verify everything looks OK
8. Publish the deployment

## Update the Site

The site will publish automatically as part of the release process. But you can do it independently too.

The following command will do a dry run of the site publishing process - you can use it to see what will be published:

```shell
./mvnw clean site scm-publish:publish-scm -Dscmpublish.dryRun=true
```

This command will publish the site:

```shell
./mvnw clean site scm-publish:publish-scm
```

If you run into issues with the publishing process, then resetting the working directory can help:

```shell
cd ~
sudo rm -r maven-sites
```

## After Releasing

Draft a new release on GitHub and tie it to the new release tag.
