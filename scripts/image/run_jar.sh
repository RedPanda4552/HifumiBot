#!/bin/bash

# TODO - how much RAM does the bot actually need?
JAVA_OPTS="-Xms100M -Xmx400M"

# TODO - eventually make sure we can logs easily out of the container (push to honeycomb.io probably)

ls -l /opt

# Run the jar
java -jar $JAVA_OPTS /opt/hifumi.jar $DISCORD_BOT_TOKEN $SUPERUSER_ID
