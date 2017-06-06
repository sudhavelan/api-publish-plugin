package com.softwareag.apiportal.api.publish.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import java.io.File;
import java.net.URL;
import java.util.Properties;
import java.util.Set;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Goal which publish an API to the APIPortal.
 *
 *
 */
@Mojo(name = "publish", defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, threadSafe = true)
public class APIPortalPublish
        extends AbstractMojo {

    @Parameter(required = true, defaultValue = "http://localhost")
    private URL portalHost;

    @Parameter(defaultValue = "/abs/apirepository/v1/apis")
    private String resource;

    @Parameter
    private Properties queryParam;

    public void setPortalHost(URL portalHost) {
        this.portalHost = portalHost;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setQueryParam(Properties queryParam) {
        this.queryParam = queryParam;
    }

//    public void setUsername(String username) {
//        this.username = username;
//    }
    public void setPassword(String password) {
        this.password = password;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setFilePath(File filePath) {
        this.filePath = filePath;
    }

//    @Parameter(defaultValue = "system")
    /**
     *
     * @parameter default-value="system"
     *
     */
    @Parameter(required = true, defaultValue = "system")
    private String username;

    @Parameter(required = true, defaultValue = "manager")
    private String password;

    @Parameter(defaultValue = "multipart/form-data")
    private String contentType;

    @Parameter(required = true, defaultValue = "${project.build.directory}/swagger.json")
    private File filePath;

    public URL getPortalHost() {
        return portalHost;
    }

    public String getResource() {
        return resource;
    }

    public Properties getQueryParam() {
        return queryParam;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getContentType() {
        return contentType;
    }

    public File getFilePath() {
        return filePath;
    }

    /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @Parameter(required = true, defaultValue = "${project.build.directory}")
    private File outputDirectory;

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void execute()
            throws MojoExecutionException {
        getLog().debug(" the output " + outputDirectory);
        getLog().debug("The content-type " + getContentType());
        getLog().debug("The url " + getPortalHost());
        final Properties qParam = getQueryParam();
        final Client client = Client.create();
        final Properties prop = new Properties();
        String queryString =  processQueryParameter(qParam);
        getLog().debug("The credentials " + getUsername() + " : " + getPassword());
        getLog().debug("The resource " + getResource());
        getLog().debug("The filepath " + getFilePath());

        //Client with basic auth
        client.addFilter(new HTTPBasicAuthFilter(getUsername(), getPassword()));
        String updatePath = getApiId(prop);
        String baseurl = getPortalHost() +  getResource() ;
        baseurl = (updatePath.isEmpty() ? baseurl : baseurl+"/"+updatePath);
        getLog().debug("The request url " + baseurl);

        String path = baseurl + queryString;
        getLog().info("Web path to publish :" + path);
        WebResource webResource = client.resource(path);
        //File to Multipart form data
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        File created_File = filePath;
        formDataMultiPart.bodyPart(new FileDataBodyPart("api-package", created_File));
        ClientResponse response = webResource.type(getContentType()).accept("*/*").post(ClientResponse.class, formDataMultiPart);
        updateResponse(response, prop);
        
    }

    /**
     *  Process query param 
     * @param qParam - query parameter configured in the pom.xml
     */
    private String processQueryParameter( final Properties qParam) {
        final StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("?");
        if (qParam != null) {
            getLog().debug("The query parameters are :");
            Set<Object> keySet = qParam.keySet();
            for (Object object : keySet) {
                String key = (String) object;
                String value = qParam.getProperty((String) object);
                getLog().debug("Key - " + object + " : " + value);
                //key=value&
                queryBuilder.append(key).append("=").append(value).append("&");
            }
        } else {
            queryBuilder.append("tenant=default&type=Swagger&");
        }
        queryBuilder.append("async=false");// always async false
        return queryBuilder.toString();
    }

    /**
     *  Update the properties and save it in new config file
     * @param response - Client response from the api portal
     * @param prop - properties 
     */
    private void updateResponse(ClientResponse response, final Properties prop){
        if (response.getStatus() == 200) {
            getLog().info("Publish success");
            String responseFromServer = response.getEntity(String.class);
            getLog().info("The config file name is " + getTempConfigFile().getName());
            String apiID = parseAndFetchApiId(responseFromServer);
            prop.setProperty("apiID", apiID);
            try (OutputStream os = new FileOutputStream(getTempConfigFile());) {
                prop.store(os, "store API ID");
            } catch (IOException e) {
                getLog().warn("Saving api ID failed");
                getLog().warn(e);
            }
        } else {
            getLog().error("Publish failed status :" + response.getStatus());
        }
    }

    
    /**
     *  Parse and fetch the api id
     * @param response - response string from the server
     * @return api id
     */
    private String parseAndFetchApiId(String response){
        //UUID pattern
        final String regex = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(response);
        String api="";
        System.out.println("The response is "+response);
        if(matcher.find()) {
            api = matcher.group();
        }
        System.out.println("the api is "+api);
        return api;
    }
    
    /**
     * Returns the apiId if it is already published. first time executing will return empty string
     * @param prop - properties to load the config
     * @return 
     */
    private String getApiId(Properties prop) {
       
     try (FileInputStream input = new FileInputStream(getTempConfigFile());){
            // load a properties file
            prop.load(input);
        } catch (IOException ex) {
            //TODO: ignore
            getLog().warn("Error navigating the file "+getTempConfigFile().getAbsolutePath());
            getLog().warn("if your are seeing this first time igore this error ");
        }
        String updatePath = prop.getProperty("apiID", "");
        if(!updatePath.isEmpty()){
        getLog().info("The api id is "+updatePath);
        }
        else{
            getLog().debug("The api id is empty. Might be this is first time you are executing");
        }
        return updatePath;
    }
    
    /**
     * Save the new config file in the build target directory
     * @return 
     */
    private File getTempConfigFile(){
        File f = outputDirectory;
        File configFile = new File(f, getTempConfigName());
        return configFile;
    }
    /**
     * create new file name based on the input for example 
     * c:/test/swagger.json is the the input for filePath then this method
     * returns swagger.json.config
     * @return 
     */
    private String getTempConfigName(){
        return filePath.getName()+".config";
    }
}
