Official Docker image for [it-at-m/ad2image](https://github.com/it-at-m/ad2image).

ad2image provides an easy-to-use, minimalistic HTTP API to retrieve user photos from an Active Directory / Microsoft Exchange environment. It also provides fallback photos using [avatar-generator](https://gitlab.talanlabs.com/gabriel-allaigre/avatar-generator-parent) if a user has no photo.

Behind the curtains, by default a 64x64 pixel thumbnail photo is retrieved from Active Directory (Attribute `thumbnailPhoto`). Higher resolution photos can also be used, those will be fetched internally by using the [Exchange EWS REST API](https://learn.microsoft.com/en-us/exchange/client-developer/exchange-web-services/how-to-get-user-photos-by-using-ews-in-exchange#get-a-mailbox-user-photo-by-using-rest).

## Usage

Please see GitHub README.md for documentation: <https://github.com/it-at-m/ad2image>
