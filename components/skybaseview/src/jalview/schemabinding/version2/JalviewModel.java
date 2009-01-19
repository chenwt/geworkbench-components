/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: JalviewModel.java,v 1.1 2009-01-19 02:28:35 wangm Exp $
 */

package jalview.schemabinding.version2;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.Date;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

/**
 * Class JalviewModel.
 * 
 * @version $Revision: 1.1 $ $Date: 2009-01-19 02:28:35 $
 */
public class JalviewModel implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _creationDate
     */
    private java.util.Date _creationDate;

    /**
     * Field _version
     */
    private java.lang.String _version;

    /**
     * Field _vamsasModel
     */
    private jalview.schemabinding.version2.VamsasModel _vamsasModel;

    /**
     * Field _jalviewModelSequence
     */
    private jalview.schemabinding.version2.JalviewModelSequence _jalviewModelSequence;


      //----------------/
     //- Constructors -/
    //----------------/

    public JalviewModel() {
        super();
    } //-- jalview.schemabinding.version2.JalviewModel()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'creationDate'.
     * 
     * @return Date
     * @return the value of field 'creationDate'.
     */
    public java.util.Date getCreationDate()
    {
        return this._creationDate;
    } //-- java.util.Date getCreationDate() 

    /**
     * Returns the value of field 'jalviewModelSequence'.
     * 
     * @return JalviewModelSequence
     * @return the value of field 'jalviewModelSequence'.
     */
    public jalview.schemabinding.version2.JalviewModelSequence getJalviewModelSequence()
    {
        return this._jalviewModelSequence;
    } //-- jalview.schemabinding.version2.JalviewModelSequence getJalviewModelSequence() 

    /**
     * Returns the value of field 'vamsasModel'.
     * 
     * @return VamsasModel
     * @return the value of field 'vamsasModel'.
     */
    public jalview.schemabinding.version2.VamsasModel getVamsasModel()
    {
        return this._vamsasModel;
    } //-- jalview.schemabinding.version2.VamsasModel getVamsasModel() 

    /**
     * Returns the value of field 'version'.
     * 
     * @return String
     * @return the value of field 'version'.
     */
    public java.lang.String getVersion()
    {
        return this._version;
    } //-- java.lang.String getVersion() 

    /**
     * Method isValid
     * 
     * 
     * 
     * @return boolean
     */
    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * Method marshal
     * 
     * 
     * 
     * @param out
     */
    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * Method marshal
     * 
     * 
     * 
     * @param handler
     */
    public void marshal(org.xml.sax.ContentHandler handler)
        throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, (java.io.Writer)handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Sets the value of field 'creationDate'.
     * 
     * @param creationDate the value of field 'creationDate'.
     */
    public void setCreationDate(java.util.Date creationDate)
    {
        this._creationDate = creationDate;
    } //-- void setCreationDate(java.util.Date) 

    /**
     * Sets the value of field 'jalviewModelSequence'.
     * 
     * @param jalviewModelSequence the value of field
     * 'jalviewModelSequence'.
     */
    public void setJalviewModelSequence(jalview.schemabinding.version2.JalviewModelSequence jalviewModelSequence)
    {
        this._jalviewModelSequence = jalviewModelSequence;
    } //-- void setJalviewModelSequence(jalview.schemabinding.version2.JalviewModelSequence) 

    /**
     * Sets the value of field 'vamsasModel'.
     * 
     * @param vamsasModel the value of field 'vamsasModel'.
     */
    public void setVamsasModel(jalview.schemabinding.version2.VamsasModel vamsasModel)
    {
        this._vamsasModel = vamsasModel;
    } //-- void setVamsasModel(jalview.schemabinding.version2.VamsasModel) 

    /**
     * Sets the value of field 'version'.
     * 
     * @param version the value of field 'version'.
     */
    public void setVersion(java.lang.String version)
    {
        this._version = version;
    } //-- void setVersion(java.lang.String) 

    /**
     * Method unmarshal
     * 
     * 
     * 
     * @param reader
     * @return Object
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (jalview.schemabinding.version2.JalviewModel) Unmarshaller.unmarshal(jalview.schemabinding.version2.JalviewModel.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     * 
     */
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
