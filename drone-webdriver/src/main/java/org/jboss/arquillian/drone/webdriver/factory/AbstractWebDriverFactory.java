package org.jboss.arquillian.drone.webdriver.factory;

import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilitiesRegistry;
import org.openqa.selenium.WebDriver;

abstract class AbstractWebDriverFactory<T extends WebDriver> implements Configurator<T, WebDriverConfiguration> {

    private static final Logger log = Logger.getLogger(AbstractWebDriverFactory.class.getName());

    protected abstract String getDriverReadableName();

    @Inject
    protected Instance<BrowserCapabilitiesRegistry> registryInstance;

    @Override
    public WebDriverConfiguration createConfiguration(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier) {

        BrowserCapabilitiesRegistry registry = registryInstance.get();

        // first, try to create a BrowserCapabilities object based on Field/Parameter type of @Drone annotated field
        BrowserCapabilities browser = registry.getEntryFor(getDriverReadableName());
        WebDriverConfiguration configuration = new WebDriverConfiguration(browser).configure(descriptor, qualifier);

        // then, check if legacy implementationClass was set in the configuration and try to update accordingly
        if (browser == null && Validate.nonEmpty(configuration.getImplementationClass())) {
            browser = registry.getEntryByImplementationClassName(configuration.getImplementationClass());
            if (browser == null) {
                log.log(Level.FINE, "Available implementationClasses are {}", getAvailableImplementationClasses());
                throw new IllegalStateException(
                        MessageFormat
                                .format("Unable to initialize WebDriver instance. Please specify a browserCapabilities property instead of implementationClass {1}. Available options are: {0}",
                                        getAvailableBrowserCapabilities(), configuration.getImplementationClass()));
            }
            configuration.setBrowserCapabilitiesInternal(browser);
            log.log(Level.WARNING,
                    "Please use browserCapability to specify browser type instead of implementationClass. Available options are: {0}",
                    getAvailableBrowserCapabilities());
        }
        // otherwise, we hit a webdriver configuration and we want to use browserCapabilities
        if (browser == null && Validate.nonEmpty(configuration.getBrowserCapabilities())) {
            browser = registry.getEntryFor(configuration.getBrowserCapabilities());
            if (browser == null) {
                throw new IllegalStateException(
                        MessageFormat
                                .format("Unable to initialize WebDriver instance. Please specify a valid browserCapabilities instead of {1}. Available options are: {0}",
                                        getAvailableBrowserCapabilities(), configuration.getBrowserCapabilities()));
            }
            configuration.setBrowserCapabilitiesInternal(browser);
        }

        // if it is still null, go with defaults
        if (browser == null) {
            browser = registry.getEntryFor(WebDriverConfiguration.DEFAULT_BROWSER_CAPABILITIES);
            log.log(Level.INFO, "Property \"browserCapabilities\" was not specified, using default value of {0}",
                    WebDriverConfiguration.DEFAULT_BROWSER_CAPABILITIES);
            configuration.setBrowserCapabilitiesInternal(browser);
        }

        return configuration;
    }

    private String getAvailableBrowserCapabilities() {

        BrowserCapabilitiesRegistry registry = registryInstance.get();

        StringBuilder sb = new StringBuilder();
        for (BrowserCapabilities browser : registry.getAllBrowserCapabilities()) {
            if (Validate.nonEmpty(browser.getReadableName())) {
                sb.append(browser.getReadableName()).append(", ");
            }
        }
        // trim
        if (sb.lastIndexOf(", ") != -1) {
            sb.delete(sb.length() - 2, sb.length());
        }

        return sb.toString();
    }

    private String getAvailableImplementationClasses() {

        BrowserCapabilitiesRegistry registry = registryInstance.get();

        StringBuilder sb = new StringBuilder();
        for (BrowserCapabilities browser : registry.getAllBrowserCapabilities()) {
            sb.append(browser.getImplementationClassName()).append(", ");
        }
        // trim
        if (sb.lastIndexOf(", ") != -1) {
            sb.delete(sb.length() - 2, sb.length());
        }

        return sb.toString();
    }
}
