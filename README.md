# JLineup

## Status

[![Travis CI](https://travis-ci.org/otto-de/jlineup.svg?branch=master)](https://travis-ci.org/otto-de/jlineup)
[![Known Vulnerabilities](https://snyk.io/test/github/otto-de/jlineup/badge.svg?targetFile=build.gradle)](https://snyk.io/test/github/otto-de/jlineup?targetFile=build.gradle)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.otto/jlineup-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.otto/jlineup)
<!--- [![Dependency Status](https://www.versioneye.com/user/projects/58175e12d33a712754f2ab3d/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/58175e12d33a712754f2ab3d)) -->

## About
JLineup is a tool which is useful for visual acceptance tests, especially in continuous delivery pipelines.
It can be used as a simple command line tool or as a small web service which is controlled via REST API.

JLineup can make and compare screenshots of a web page. Through comparison of the screenshots it detects every changed pixel.
JLineup generates a HTML report and a JSON report.
Behind the scenes, it uses Selenium and a browser of choice (currently Chrome, Firefox and
PhantomJS¹ are supported).

JLineup is a configuration compatible replacement
for Lineup, implemented in Java. The original
[Lineup](https://github.com/otto-de/lineup) is
a Ruby tool, but is not maintained any more.

Credit for original Lineup goes to [Finn Lorbeer](http://www.lor.beer/).

## Basic concept

This is a quick example, how JLineup can be helpful in your automated build and deploy pipeline.
Let's assume, this is part of a continuous integration pipeline:

![Pipeline exampe](docs/pipeline-example.png)

## Quick Howto

JLineup CLI comes as executable Java Archive. Java 8 or higher has to be available to run it.

Open a terminal and download it like this:

    wget https://oss.sonatype.org/service/local/artifact/maven/redirect\?r\=releases\&g\=de.otto\&a\=jlineup-cli\&v\=3.0.0-rc7\&e\=jar -O jlineup.jar

Then type

    java -jar jlineup.jar --help
  
to see the command line help.

## Browser compatibility

JLineup 3.0.0-rc7 was tested successfully with

* Chrome 71.x
* Firefox 62.x
* PhantomJS 2.1.1 (auto-downloaded by JLineup if not installed)
        
Chrome or Firefox have to be installed on the system if you want to use one of them.

## Documentation

[JLineup Job Configuration](docs/CONFIGURATION.md)

## Third party libraries

JLineup uses some third party tools and libraries

##### Selenium

* [Selenium](http://www.seleniumhq.org/) is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0)

##### Webdrivermanager

* [Webdrivermanager](https://github.com/bonigarcia/webdrivermanager) is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0)
  
##### JCommander

* [JCommander](http://jcommander.org/) is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0)

##### Gson

* [google-gson](https://github.com/google/gson) is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0) 

##### Logback

* [Logback](http://logback.qos.ch/) is licensed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html)
* The [SLF4J](http://www.slf4j.org) API is licensed under the [MIT License](http://www.slf4j.org/license.html)

##### PhantomJS

* [PhantomJS](http://phantomjs.org/) is licensed under the [BSD-3-Clause](https://github.com/ariya/phantomjs/blob/master/LICENSE.BSD)

##### Thymeleaf

* [Thymeleaf](http://www.thymeleaf.org/) is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0)


### Contact

If you have questions or proposals, please open an issue or write an email to marco DOT geweke AT otto.de

### Footnotes

¹) PhantomJS Development has been suspended. For more details go to https://github.com/ariya/phantomjs/issues/15344

