#!/bin/bash
set -e
mvn clean site site:stage scm-publish:publish-scm