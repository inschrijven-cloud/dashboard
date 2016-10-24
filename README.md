# Speelsysteem
[![Build Status](https://travis-ci.org/speelsysteem/dashboard.svg?branch=master)](https://travis-ci.org/speelsysteem/dashboard)
[![Coverage Status](https://coveralls.io/repos/github/speelsysteem/dashboard/badge.svg?branch=master)](https://coveralls.io/github/speelsysteem/dashboard?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/0927605b06c24469a5f89efc85f86a91)](https://www.codacy.com/app/toye-thomas/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=speelsysteem/dashboard&amp;utm_campaign=Badge_Grade)

This is the [speelsysteem](https://github.com/speelsysteem) dashboard API. It's a Play 2 layer over a CouchDB database.

## Functionality

### Child API

| Implemented | Description                       | Endpoint                               |
|-------------|-----------------------------------|----------------------------------------|
| X           | Create new child                  | `POST /api/v1/child`                   |
| X           | Retrieve all children             | `GET /api/v1/child`                    |
| X           | Retrieve a child by id            | `GET /api/v1/child/:id`                |
|             | Update child                      | `PUT /api/v1/child/:id`                |
| X           | Delete child                      | `DELETE /api/v1/child/:id`             |


### Crew API

| Implemented | Description                       | Endpoint                               |
|-------------|-----------------------------------|----------------------------------------|
|             | Create new crew member            | `POST /api/v1/crew`                    |
|             | Retrieve all crew members         | `GET /api/v1/crew`                     |
|             | Retrieve a crew member by id      | `GET /api/v1/crew/:id`                 |
|             | Update crew member                | `PUT /api/v1/crew`                     |
|             | Delete crew member                | `DELETE /api/v1/crew`                  |


### Day and shift api

| Implemented | Description                                        | Endpoint                               |
|-------------|----------------------------------------------------|----------------------------------------|
|             | Create day                                         | `POST /api/v1/day`                     |
|             | Create shift on a day                              |  |
| X           | Retrieve all days                                  | `GET /api/v1/day`                      |
|             | Retrieve all shifts on a day                       |  |
|             | Retrieve a day by  id                              | `GET /api/v1/day/:id`                  |
|             | Update a day                                       | `PUT /api/v1/day`                      |
|             | Update shift of a day                              |  |


### Attendance API

#### Child attendance API

| Implemented | Description                                        | Endpoint                                           |
|-------------|----------------------------------------------------|----------------------------------------------------|
| X           | Number of child attendances on a day per shift     | `GET /api/v1/day/attendances/child`                |
|             | Retrieve children on a day per shift               | `GET /api/v1/day/:id/attendances/child`            |
| X           | Create attendance for a child                      | `POST /api/v1/child/:childId/attendances/:dayId`   |
|             | Delete attandance for a child                      | `DELETE /api/v1/child/:childId/attendances/:dayId` |
| X           | Retrieve attended days with shifts for child       | `GET /api/v1/child/:id/attendances`                |


#### Crew attendance API


| Implemented | Description                                        | Endpoint                                         |
|-------------|----------------------------------------------------|--------------------------------------------------|
|             | Retrieve crew members on a day per shift           | |
|             | Create attendance for a crew member                | |
|             | Delete attendance for a crew member                | |
|             | Number of crew attendances on a day per shift      | |
|             | Retrieve attended days with shifts for crew member | |


### Report API

| Implemented | Description                                       | Endpoint                                             |
|-------------|---------------------------------------------------|------------------------------------------------------|
| X           | Generate fiscal certificate data                  | `GET /api/v1/files/fiscalCertificate/:year`          |
|             | Generate fiscal certificate for a child           | `GET /api/v1/files/fiscalCertificate/:year/:childId` |
|             | Generate compensation certificate data            | `GET /api/v1/files/compensation/:year`               |
|             | Generate compensation certificate for a volunteer | `GET /api/v1/files/compensation/:year/:crewId`       |


### Export API

| Implemented | Description                              | Endpoint                               |
|-------------|------------------------------------------|----------------------------------------|
|             | List of children                         | `GET /api/v1/files/export/children`    |
|             | List of crew members                     | `GET /api/v1/files/export/crew`        |
|             | List of children with medical conditions |                                        |
