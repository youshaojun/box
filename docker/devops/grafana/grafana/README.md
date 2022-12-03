## Grafana

#### [搜索自己想要的模板](https://grafana.com/grafana/dashboards)

#### 导入一些好用的模板
- [x] Linux (10795, 8919)
- [x] JVM (10280, 4701)
- [x] Redis (11835)
- [x] MySQL (7362)
- [x] Nginx (2949, 12559)

> 插件
```shell
docker exec -it grafana /bin/sh
cd bin

# 安装插件
grafana-cli plugins install grafana-worldmap-panel

# 卸载插件
grafana-cli plugins uninstall grafana-worldmap-panel

docker restart grafana
```
