# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## 1.2.0

- Add `isLocalImage` to the builder to specify image which should not be pulled from the registry (#4 by @fagossa)
- Upgrade to com.spotify:docker-client:8.9.1 
⚠️ We no longer use the shaded version because of https://github.com/spotify/docker-client/issues/900
- Fix default environment (it now works with docker for mac)

## 1.1.0

- Add a builder to remove the need for inheritance
- Add a waitForLog directive

## 1.0.2
> 2016-01-06

- Include guava in the dependencies

## 1.0.1
> 2016-01-06

- Use the shaded jar of the docker-client

## 1.0.0
> 2016-01-06

- Initial release
