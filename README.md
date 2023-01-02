<div id="top"></div>

<!-- PROJECT SHIELDS -->

[![Contributors][contributors-shield]][contributors-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]
[![GitHub Workflow Status][github-workflow-status]][github-workflow-status-url]
[![GitHub release (latest SemVer)][release-shield]][release-url]

<!-- END OF PROJECT SHIELDS -->

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="#">
    <img src="images/logo.png" alt="Logo" height="200">
  </a>

<h3 align="center">ad2image</h3>

  <p align="center">
    <i>easy avatars powered by AD & Identicons</i>
    <br /><a href="https://github.com/it-at-m/ad2image/issues/new?assignees=&labels=&template=bug_report.md&title=">Report Bug</a>
    ·
    <a href="https://github.com/it-at-m/ad2image/issues/new?assignees=&labels=&template=feature_request.md&title=">Request Feature</a>
  </p>
</div>

<!-- ABOUT THE PROJECT -->

## About The Project

**ad2image** provides an easy-to-use, minimalistic HTTP API to retrieve user photos from an Active Directory / Microsoft Exchange environment. It also provides fallback photos using [avatar-generator](https://gitlab.talanlabs.com/gabriel-allaigre/avatar-generator-parent) if a user has no photo.

Behind the curtains, by default a 64x64 pixel thumbnail photo is retrieved from Active Directory (Attribute `thumbnailPhoto`). Higher resolution photos can also be used, those will be fetched internally by using the [Exchange EWS REST API](https://learn.microsoft.com/en-us/exchange/client-developer/exchange-web-services/how-to-get-user-photos-by-using-ews-in-exchange#get-a-mailbox-user-photo-by-using-rest).

**ad2image** can be integrated in your Spring Boot application by using the provided starter `ad2image-spring-boot-starter`.
If you want to deploy **ad2image** as a standalone application, you can use the provided container image.

**ad2image** was initially created as **mucatar** internally at it@M. It is used by many of our in-house projects to provide user photos for a richer user experience. For example, it can be very easily used as photo provider for [Vuetify's Avatar component](https://vuetifyjs.com/en/components/avatars/).

<p align="right">(<a href="#top">back to top</a>)</p>

### Built With

This project is built with technologies we use in our projects:

- Java
- Spring Boot

<p align="right">(<a href="#top">back to top</a>)</p>

## Set up

```bash
mvn clean install
```

For development, a Active Directory / Exchange environment is not needed. It is mocked by an embedded LDAP server and WireMock (for the EWS API).

<p align="right">(<a href="#top">back to top</a>)</p>

## Documentation

### Using the API

`GET /avatar?uid=john.doe[&m=identicon|404|fallbackIdenticon][&size=64]`

Possible modes (`m`):

- `404`: 404 Response, if the user has no photo stored in AD/Exchange
- `identicon`: **default** - renders an [Identicon](https://en.wikipedia.org/wiki/Identicon), if the user has no photo stored in AD/Exchange
- `fallbackIdenticon`: identical to `identicon`, but also responds with an Identicon if the user itself does not exist in AD/Exchange
- `generic`: renders an [generic placeholder icon](ad2image-spring-boot-starter/src/main/resources/account_64.png), if the user has no photo stored in AD/Exchange
- `fallbackGeneric`: identical to `generic`, but also responds with an generic placeholder icon if the user itself does not exist in AD/Exchange
- `triangle`: renders an [randomly generated Avatar based on triangles](https://raw.githubusercontent.com/gabrie-allaigre/avatar-generator/master/doc/triangle1.png), if the user has no photo stored in AD/Exchange
- `fallbackTriangle`: identical to `triangle`, but also responds correspondingly if the user itself does not exist in AD/Exchange
- `square`: renders an [randomly generated Avatar based on squares](https://raw.githubusercontent.com/gabrie-allaigre/avatar-generator/master/doc/square1.png), if the user has no photo stored in AD/Exchange
- `fallbackSquare`: identical to `square`, but also responds correspondingly if the user itself does not exist in AD/Exchange
- `github`: renders an [randomly generated Avatar based Github avatar style](https://raw.githubusercontent.com/gabrie-allaigre/avatar-generator/master/doc/github2.png), if the user has no photo stored in AD/Exchange
- `fallbackGithub`: identical to `github`, but also responds correspondingly if the user itself does not exist in AD/Exchange

Possible resolutions (`size`):

- `64` (default)
- `96`
- `120`
- `240`
- `360`
- `432`
- `504`
- `648`

### Running as a container (standalone)

:construction: TODO

### Integrating in a existing Spring Boot application

:construction: TODO

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- CONTRIBUTING -->

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please open an issue with the tag "enhancement", fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Open an issue with the tag "enhancement"
2. Fork the Project
3. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
4. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
5. Push to the Branch (`git push origin feature/AmazingFeature`)
6. Open a Pull Request

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- LICENSE -->

## License

Distributed under the MIT License. See `LICENSE` file for more information.

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- CONTACT -->

## Contact

it@m - opensource@muenchen.de

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->

[contributors-shield]: https://img.shields.io/github/contributors/it-at-m/ad2image.svg?style=for-the-badge
[contributors-url]: https://github.com/it-at-m/ad2image/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/it-at-m/ad2image.svg?style=for-the-badge
[forks-url]: https://github.com/it-at-m/ad2image/network/members
[stars-shield]: https://img.shields.io/github/stars/it-at-m/ad2image.svg?style=for-the-badge
[stars-url]: https://github.com/it-at-m/ad2image/stargazers
[issues-shield]: https://img.shields.io/github/issues/it-at-m/ad2image.svg?style=for-the-badge
[issues-url]: https://github.com/it-at-m/ad2image/issues
[license-shield]: https://img.shields.io/github/license/it-at-m/ad2image.svg?style=for-the-badge
[license-url]: https://github.com/it-at-m/ad2image/blob/main/LICENSE
[github-workflow-status]: https://img.shields.io/github/actions/workflow/status/it-at-m/ad2image/build.yaml?style=for-the-badge
[github-workflow-status-url]: https://github.com/it-at-m/ad2image/actions/workflows/build.yaml
[release-shield]: https://img.shields.io/github/v/release/it-at-m/ad2image?sort=semver&style=for-the-badge
[release-url]: https://github.com/it-at-m/ad2image/releases
