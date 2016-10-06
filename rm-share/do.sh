mvn clean package -Punpack-deps -Ddependency.surf.version=6.3
scp target/rm-share.amp flemming@staging.rm.magenta.dk:~
