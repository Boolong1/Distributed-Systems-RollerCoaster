# Important Commands

## CleanUp
```shell
pkill -f rollerCoaster.PassengerClient; pkill -f wagon_client.py
rm -f logs/*.log
```

## start Station (Terminal 1)
```shell
java -cp "build:lib/*" rollerCoaster.StationServer 4
```

## Start Wagon(s) (Terminal 2)
```shell
./StartWagons.sh localhost 4
```

## Start Passenger(s) (Terminal 3)
```shell
./StartPassengers.sh localhost 10
```

## Live Logs (Terminal 3)
```shell
tail -F logs/*.log
```
