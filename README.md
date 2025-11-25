# HifumiBot

## Development (Windows)

Dependencies (using https://scoop.sh/)

```bash
scoop bucket add java
scoop install openjdk25 maven
mvn install
```

## Docker (locally)

- Get Docker (not the place to explain how to do this)
- Build the container locally:
  - `docker build -t hifumi:local .`
- Run it via docker-compose:
  - `DISCORD_BOT_TOKEN=TOKEN_HERE SUPERUSER_ID=ID_HERE DEEPL_KEY=KEY_HERE docker compose -f ./docker-compose.local.yaml up`