![Cognifide logo](http://cognifide.github.io/images/cognifide-logo.png)

[![][travis img]][travis]
[![][sonarqube img]][sonarqube]
[![][license img]][license]

#Knot.x - efficient, high-performance and scalable integration platform for modern websites   

<p align="center">
  <img src="https://github.com/Cognifide/knotx/blob/master/icons/180x180.png?raw=true" alt="Knot.x Logo"/>
</p>


## What is Knot.x?
Let's imagine **an online banking website** containing different features like a *chat box*, *exchange rates*, 
*stock data* and *user profile information*. The site has a high performance characteristic, despite of 
its complexity and *target publishing channels*.

All those features come from different providers/vendors having their own teams working in various modes, 
technologies and release cycles.

**Knot.x** connects all of them in a controlled and isolated way, preventing any undesired interferences.
It combines **asynchronous programming principles** and **message-driven architecture** providing **a scalable 
platform** for modern sites.

**Knot.x** connects all of the above **in a unified customer experience**.


## What problems does Knot.x solve?

###Features

<img align="right" 
  src="https://github.com/Cognifide/knotx/blob/master/documentation/src/main/wiki/assets/knotx-intro-features.png?raw=true"
  alt="Knot.x Features"/>

Probably you have many features / services you want to connect to your site. They come from 
different vendors, talk using various network protocols (HTTP/WebSocket/TCP etc.) and message 
formats (SOAP/JSON/XML/binary etc.).

**Knot.x** assembles your static HTML pages with any features / services in a very 
performant manner. It loads and analyses static pages from a Repository, collects dynamic 
features from multiple sources asynchronously and injects them into the page.
If a service you connect to may have unpredictable or cyclic outages you can easily handle them according
to your business rules. 

Read the [Service Knot](https://github.com/Cognifide/knotx/wiki/ServiceKnot) 
section to find out more about this topic.

###Forms

<img align="left" 
  src="https://github.com/Cognifide/knotx/blob/master/documentation/src/main/wiki/assets/knotx-intro-forms.png?raw=true"
  alt="Knot.x Forms"/>

Every site contains more or less complicated forms. **Knot.x** supports simple and multi-step forms. 
It handles submission errors, form validation and redirects to success pages. 

Forms can be used to compose multi-step workflows. **Knot.x** allows you to define a graph of interconnected steps, responding to user input / site visitor choices.

Find out more about this topic by reading the [Action Knot](https://github.com/Cognifide/knotx/wiki/ActionKnot) 
section of the **Knot.x** documentation.

###Prototyping

<img align="right" 
  src="https://github.com/Cognifide/knotx/blob/master/documentation/src/main/wiki/assets/knotx-intro-prototyping.png?raw=true"
  alt="Knot.x Prototyping"/>

A potential client asked you to prepare a demo presenting the capabilities of a new site. The client
operates in the financial sector so your site needs to connect to exchange rates and stock data 
services. Those features are not publicly available so you only have some sample data to work with.

**Knot.x** gives you the ability to use simple Mocks. This allows you to expose your sample data directly to
pages. Additionally your demo pages can be easily changed to use live services without any further
development work. Your client will be impressed with how fast it can happen.

Find out more about this topic by reading the [Mocks](https://github.com/Cognifide/knotx/wiki/Mocks) 
section of this documentation.

###Extensions

<img align="left" 
  src="https://github.com/Cognifide/knotx/blob/master/documentation/src/main/wiki/assets/knotx-intro-extensions.png?raw=true"
  alt="Knot.x Extensions"/>

You need to implement custom authentication mechanism for your site and then integrate with service 
talking with its own custom protocol. **Knot.x** is a fully modular platform with very flexible extension
points that we call [Knots](https://github.com/Cognifide/knotx/wiki/Knot) and [Adapters](https://github.com/Cognifide/knotx/wiki/Adapter).

Those extension points communicates with Knot.x Core using a very performant Event Bus so you can
implement your integration layer in one place inside **Knot.x**. Not enough? If you wish, you can implement
your extensions in any language you like, as long as it's supported by [Vert.x](http://vertx.io/).

Find out more about this topic by reading the [Knots](https://github.com/Cognifide/knotx/wiki/Knot) and [Adapters](https://github.com/Cognifide/knotx/wiki/Adapter) 
sections of this documentation.

## What's the philosophy behind Knot.x?
We care a lot about speed and that is why we built **Knot.x** on top of [Vert.x](http://vertx.io/), known as one of the leading frameworks for performant, event-driven applications.

### Stability and responsiveness
**Knot.x** uses asynchronous programming principles, which allows it to process a large number of requests using a single thread.
Asynchronous programming is a style promoting the ability to write non-blocking code (no thread pools).
The platform stays responsive under heavy and varying loads and is designed to follow the principles outlined in the [Reactive Manifesto](http://www.reactivemanifesto.org/).

### Loose coupling
**Knot.x** relies on asynchronous message-passing to establish a boundary between system components that ensures 
loose coupling, isolation and location transparency. A base **Knot.x** component is called a [Knot](https://github.com/Cognifide/knotx/wiki/Knot).

### Scalability
Various scaling options are available to suit client needs and help in cost optimisation. Using a 
simple concurrency model and a message bus, **Knot.x** can be scaled within a single host or a cluster of 
servers.

## Full Documentation

See our [Wiki](https://github.com/Cognifide/knotx/wiki) for full documentation, examples and other information.


## Communication, bugs and feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/Cognifide/knotx/issues).


## Demo

You can run a **Knot.x** demo within less than 5 minutes, all you need is Java 8. See [how you can run the Knot.x demo](https://github.com/Cognifide/knotx/wiki/RunningTheDemo).


## Licence

**Knot.x** is licensed under the [Apache License, Version 2.0 (the "License")](https://www.apache.org/licenses/LICENSE-2.0.txt)


[travis]:https://travis-ci.org/Cognifide/knotx
[travis img]:https://travis-ci.org/Cognifide/knotx.svg?branch=master

[license]:LICENSE
[license img]:https://img.shields.io/badge/License-Apache%202-blue.svg

[sonarqube]:https://sonarqube.com/dashboard/index/io.knotx:knotx-root
[sonarqube img]:https://sonarqube.com/api/badges/gate?key=io.knotx:knotx-root
