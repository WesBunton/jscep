package org.jscep.message;

import static org.spongycastle.cms.CMSAlgorithm.DES_CBC;
import static org.spongycastle.cms.CMSAlgorithm.DES_EDE3_CBC;
import static org.spongycastle.cms.CMSAlgorithm.AES128_CBC;
import static org.spongycastle.cms.CMSAlgorithm.AES192_CBC;
import static org.spongycastle.cms.CMSAlgorithm.AES256_CBC;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.spongycastle.cms.CMSEnvelopedData;
import org.spongycastle.cms.CMSEnvelopedDataGenerator;
import org.spongycastle.cms.CMSException;
import org.spongycastle.cms.CMSProcessableByteArray;
import org.spongycastle.cms.CMSTypedData;
import org.spongycastle.cms.RecipientInfoGenerator;
import org.spongycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.spongycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.spongycastle.operator.OutputEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used for enveloping and encrypting a <tt>messageData</tt> to
 * produce the <tt>pkcsPkiEnvelope</tt> part of a SCEP secure message object.
 * 
 * @see PkcsPkiEnvelopeDecoder
 */
public final class PkcsPkiEnvelopeEncoder {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PkcsPkiEnvelopeEncoder.class);
    private final X509Certificate recipient;
    private final String encAlg;

    /**
     * Creates a new <tt>PkcsPkiEnvelopeEncoder</tt> for the entity identified
     * by the provided certificate.
     * 
     * @param recipient
     *            the entity for whom the <tt>pkcsPkiEnvelope</tt> is intended.
     */
    @Deprecated
    public PkcsPkiEnvelopeEncoder(final X509Certificate recipient) {
        this(recipient, "DES");
    }

    /**
     * Creates a new <tt>PkcsPkiEnvelopeEncoder</tt> for the entity identified
     * by the provided certificate.
     * 
     * @param recipient
     *            the entity for whom the <tt>pkcsPkiEnvelope</tt> is intended.
     * @param encAlg
     *            the encryption algorithm to use.
     */
    public PkcsPkiEnvelopeEncoder(final X509Certificate recipient,
            final String encAlg) {
        this.recipient = recipient;
        this.encAlg = encAlg;
    }

    /**
     * Encrypts and envelops the provided messageData.
     * 
     * @param messageData
     *            the message data to encrypt and envelop.
     * @return the enveloped data.
     * @throws MessageEncodingException
     *             if there are any problems encoding the message.
     */
    public CMSEnvelopedData encode(final byte[] messageData)
            throws MessageEncodingException {
        LOGGER.debug("Encoding pkcsPkiEnvelope");
        CMSEnvelopedDataGenerator edGenerator = new CMSEnvelopedDataGenerator();
        CMSTypedData envelopable = new CMSProcessableByteArray(messageData);
        RecipientInfoGenerator recipientGenerator;
        try {
            recipientGenerator = new JceKeyTransRecipientInfoGenerator(
                    recipient);
        } catch (CertificateEncodingException e) {
            throw new MessageEncodingException(e);
        }
        edGenerator.addRecipientInfoGenerator(recipientGenerator);
        LOGGER.debug(
                "Encrypting pkcsPkiEnvelope using key belonging to [dn={}; serial={}]",
                recipient.getSubjectDN(), recipient.getSerialNumber());

        OutputEncryptor encryptor;
        try {
            encryptor = getEncryptor();
        } catch (CMSException e) {
            throw new MessageEncodingException(e);
        }
        try {
            CMSEnvelopedData pkcsPkiEnvelope = edGenerator.generate(
                    envelopable, encryptor);

            LOGGER.debug("Finished encoding pkcsPkiEnvelope");
            return pkcsPkiEnvelope;
        } catch (CMSException e) {
            throw new MessageEncodingException(e);
        }
    }

    private OutputEncryptor getEncryptor() throws CMSException {
        if ("DES".equals(encAlg)) {
            return new JceCMSContentEncryptorBuilder(DES_CBC).build();
        } 
        else if("AES_128".equals(encAlg)){
        	return new JceCMSContentEncryptorBuilder(AES128_CBC).build();
        }
        else if ("AES_192".equals(encAlg)) {
            return new JceCMSContentEncryptorBuilder(AES192_CBC).build();
        }
        else if ("AES_256".equals(encAlg)) {
            return new JceCMSContentEncryptorBuilder(AES256_CBC).build();
        }
        else {
            return new JceCMSContentEncryptorBuilder(DES_EDE3_CBC).build();
        }
    }
}
