package io.swagger.codegen.languages;

import com.samskivert.mustache.Mustache;
import io.swagger.codegen.*;
import io.swagger.models.Info;
import org.yaml.snakeyaml.error.Mark;
import io.swagger.codegen.utils.Markdown;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class StaticHtml2Generator extends DefaultCodegen implements CodegenConfig {
    protected String invokerPackage = "io.swagger.client"; // default for Java and Android
    protected String phpInvokerPackage = "Swagger\\Client"; // default for PHP
    protected String packageName = "IO.Swagger"; // default for C#
    protected String groupId = "io.swagger";
    protected String artifactId = "swagger-client";
    protected String artifactVersion = "1.0.0";
    protected String jsProjectName;
    protected String jsModuleName;
    protected String perlModuleName = "WWW::SwaggerClient";
    protected String pythonPackageName = "swagger_client";

    public StaticHtml2Generator() {
        super();

        // clear import mapping (from default generator) as this generator does not use it
        // at the moment
        importMapping.clear();

        outputFolder = "assets";
        embeddedTemplateDir = templateDir = "htmlDocs2";

        defaultIncludes = new HashSet<String>();

        cliOptions.add(new CliOption("appName", "short name of the application"));
        cliOptions.add(new CliOption("appDescription", "description of the application"));
        cliOptions.add(new CliOption("infoUrl", "a URL where users can get more information about the application"));
        cliOptions.add(new CliOption("infoEmail", "an email address to contact for inquiries about the application"));
        cliOptions.add(new CliOption("licenseInfo", "a short description of the license"));
        cliOptions.add(new CliOption("licenseUrl", "a URL pointing to the full license"));
        cliOptions.add(new CliOption(CodegenConstants.INVOKER_PACKAGE, CodegenConstants.INVOKER_PACKAGE_DESC));
        cliOptions.add(new CliOption(CodegenConstants.PHP_INVOKER_PACKAGE, CodegenConstants.PHP_INVOKER_PACKAGE_DESC));
        cliOptions.add(new CliOption(CodegenConstants.PERL_MODULE_NAME, CodegenConstants.PERL_MODULE_NAME_DESC));
        cliOptions.add(new CliOption(CodegenConstants.PYTHON_PACKAGE_NAME, CodegenConstants.PYTHON_PACKAGE_NAME_DESC));
        cliOptions.add(new CliOption(CodegenConstants.PACKAGE_NAME, "C# package name"));
        cliOptions.add(new CliOption(CodegenConstants.GROUP_ID, CodegenConstants.GROUP_ID_DESC));
        cliOptions.add(new CliOption(CodegenConstants.ARTIFACT_ID, CodegenConstants.ARTIFACT_ID_DESC));
        cliOptions.add(new CliOption(CodegenConstants.ARTIFACT_VERSION, CodegenConstants.ARTIFACT_VERSION_DESC));
        
        additionalProperties.put("appName", "Swagger Sample");
        additionalProperties.put("appDescription", "A sample swagger server");
        additionalProperties.put("infoUrl", "https://helloreverb.com");
        additionalProperties.put("infoEmail", "hello@helloreverb.com");
        additionalProperties.put("licenseInfo", "All rights reserved");
        additionalProperties.put("licenseUrl", "http://apache.org/licenses/LICENSE-2.0.html");
        additionalProperties.put(CodegenConstants.INVOKER_PACKAGE, invokerPackage);
        additionalProperties.put(CodegenConstants.PHP_INVOKER_PACKAGE, phpInvokerPackage);
        additionalProperties.put(CodegenConstants.PERL_MODULE_NAME, perlModuleName);
        additionalProperties.put(CodegenConstants.PYTHON_PACKAGE_NAME, pythonPackageName);
        additionalProperties.put(CodegenConstants.PACKAGE_NAME, packageName);
        additionalProperties.put(CodegenConstants.GROUP_ID, groupId);
        additionalProperties.put(CodegenConstants.ARTIFACT_ID, artifactId);
        additionalProperties.put(CodegenConstants.ARTIFACT_VERSION, artifactVersion);

        supportingFiles.add(new SupportingFile("fonts/ApercuPro-Bold.eot", outputFolder + "/fonts", "ApercuPro-Bold.eot"));
        supportingFiles.add(new SupportingFile("fonts/ApercuPro-Bold.ttf", outputFolder + "/fonts", "ApercuPro-Bold.ttf"));
        supportingFiles.add(new SupportingFile("fonts/ApercuPro-Bold.woff", outputFolder + "/fonts", "ApercuPro-Bold.woff"));
        supportingFiles.add(new SupportingFile("fonts/ApercuPro-Bold.woff2", outputFolder + "/fonts", "ApercuPro-Bold.woff2"));

        supportingFiles.add(new SupportingFile("fonts/ApercuPro-Light.eot", outputFolder + "/fonts", "ApercuPro-Light.eot"));
        supportingFiles.add(new SupportingFile("fonts/ApercuPro-Light.ttf", outputFolder + "/fonts", "ApercuPro-Light.ttf"));
        supportingFiles.add(new SupportingFile("fonts/ApercuPro-Light.woff", outputFolder + "/fonts", "ApercuPro-Light.woff"));
        supportingFiles.add(new SupportingFile("fonts/ApercuPro-Light.woff2", outputFolder + "/fonts", "ApercuPro-Light.woff2"));

        supportingFiles.add(new SupportingFile("fonts/ApercuPro-Regular.eot", outputFolder + "/fonts", "ApercuPro-Regular.eot"));
        supportingFiles.add(new SupportingFile("fonts/ApercuPro-Regular.ttf", outputFolder + "/fonts", "ApercuPro-Regular.ttf"));
        supportingFiles.add(new SupportingFile("fonts/ApercuPro-Regular.woff", outputFolder + "/fonts", "ApercuPro-Regular.woff"));
        supportingFiles.add(new SupportingFile("fonts/ApercuPro-Regular.woff2", outputFolder + "/fonts", "ApercuPro-Regular.woff2"));

        supportingFiles.add(new SupportingFile("fonts/fonts.css", outputFolder + "/fonts", "fonts.css"));

        supportingFiles.add(new SupportingFile("index.mustache", "", "index.html"));

        instantiationTypes.put("array", "ArrayList");
        instantiationTypes.put("map", "HashMap");

        reservedWords = new HashSet<String>();
        languageSpecificPrimitives = new HashSet<String>();
        importMapping = new HashMap<String, String>();
    }
    @Override
    public CodegenType getTag() {
        return CodegenType.DOCUMENTATION;
    }

    @Override
    public String getName() {
        return "html2";
    }

    @Override
    public String getHelp() {
        return "Generates a dynamic HTML site.";
    }

    @Override
    public String escapeReservedWord(String name) {
        if(this.reservedWordsMappings().containsKey(name)) {
            return this.reservedWordsMappings().get(name);
        }
        return "_" + name;
    }

    @Override
    public String escapeQuotationMark(String input) {
        // just return the original string
        return input;
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        // just return the original string
        return input;
    }
}
