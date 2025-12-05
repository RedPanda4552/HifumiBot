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
- Put your files (db file / json files) in `data/`
- Ensure that the `data/` folder has the correct permissions for the container to use:
  - `docker run --rm  hifumi:local sh -c "id -u app; id -g app" | { read uid; read gid; sudo chown -R $uid:$gid ./data/; }`
  - Alternatively you can just make it accessible to all users/groups: `chmod -R 777 ./data`
- Run it via docker-compose:
  - `DISCORD_BOT_TOKEN=TOKEN_HERE SUPERUSER_ID=ID_HERE DEEPL_KEY=KEY_HERE docker compose -f ./docker-compose.local.yaml up`