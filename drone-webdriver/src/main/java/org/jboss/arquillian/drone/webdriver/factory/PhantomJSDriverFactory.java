/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.arquillian.drone.webdriver.factory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.PhantomJSDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.configuration.TypedWebDriverConfiguration;
import org.jboss.selenium.phantomjs.resolver.ResolvingPhantomJSDriverService;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

/**
 * @author <a href="mailto:jpapouse@redhat.com">Jan Papousek</a>
 */
public class PhantomJSDriverFactory implements Configurator<PhantomJSDriver, TypedWebDriverConfiguration<PhantomJSDriverConfiguration>>,
        Instantiator<PhantomJSDriver, TypedWebDriverConfiguration<PhantomJSDriverConfiguration>>, Destructor<PhantomJSDriver> {

    private static final Logger LOG = Logger.getLogger(TypedWebDriverConfiguration.class.getName());

    @Override
    public TypedWebDriverConfiguration<PhantomJSDriverConfiguration> createConfiguration(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier) {
        return new TypedWebDriverConfiguration<PhantomJSDriverConfiguration>(PhantomJSDriverConfiguration.class).configure(descriptor,
                qualifier);
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public PhantomJSDriver createInstance(TypedWebDriverConfiguration<PhantomJSDriverConfiguration> configuration) {
        if (configuration.isResolvePhantomjs()) {
            try {
                return new PhantomJSDriver(ResolvingPhantomJSDriverService.createDefaultService(configuration.getCapabilities()), configuration.getCapabilities());
            } catch (IOException ex) {
                Logger.getLogger(PhantomJSDriverFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new PhantomJSDriver(configuration.getCapabilities());
    }

    @Override
    public void destroyInstance(PhantomJSDriver instance) {
        instance.quit();
    }

}
