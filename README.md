api-publish-plugin
============================

A Maven plugin to publish API to the [API Portal](http://api.webmethodscloud.com)

Usage
============================

Add to your `build->plugins` section (default phase is `package` phase)
```xml
<plugin>
   <groupId>com.softwareag.apiportal</groupId>
   <artifactId>api-publish-plugin</artifactId>
   <version>1.0-SNAPSHOT</version>
   <executions>
     <execution>
       <id>publish-api-1</id>
       <phase>package</phase>
       <goals>
        <goal>publish</goal>
       </goals>
       <configuration>
         <portalHost>http://mcvde03.eur.ad.sag</portalHost>
         <filePath>C:/demo-api-edit/PortalSearchAPI_swagger.json</filePath>
       </configuration>
     </execution>
   </executions>
</plugin>
```

Followed by:

```
mvn clean compile
```

### General Configuration parameters

- `portalHost` - API Portal Host default is `http://localhost`
- `resource` - The rest api resource path default is `/abs/apirepository/v1/apis`
- `queryParam` - It is a set of property for query parameters , `tenant` - default,sagtours or custom tenants , `Type` - swagger, raml, wsdl or custom types
    ```xml
	      <queryParam>
            <property>
             <name>tenant</name>
             <value>default</value>
            </property>
			<property>
             <name>Type</name>
             <value>swagger</value>
            </property>
          </queryParam>        
	```
 
- `username` - API Portal username by default `system'
- `password` - API Portal password by default `manager`
- `filePath` - File path of the API , by default this looks for target directory swagger.json, (default is `${project.build.directory}/swagger.json`), you can specify absolute path like `c:/publish/swagger.json` or `/usr/bin/swagger.json`
- `outputDirectory` - Location from where the config files saves. default `${project.build.directory}`
- `contentType` - Content-Type this is usually multipart/form-data so far - this is an optional paramter

Support
============================
Please feel free to file a bug
