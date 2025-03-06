package io.mosip.biosdk.client.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;

public class TestUtil {
	private TestUtil() {
	}

	public static byte[] readXmlFileAsBytes(String fileName, Class className) throws IOException {
		// Use getClassLoader to access the resource file
		try (InputStream inputStream = className.getClassLoader().getResourceAsStream(fileName);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

			if (inputStream == null) {
				throw new IOException("File not found: " + fileName);
			}

			// Read the input stream into the byte array output stream
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			return outputStream.toByteArray();
		}
	}

	public static List<BIR> getBIRDataFromXMLType(byte[] xmlBytes, String type) throws Exception {
		BiometricType biometricType = null;
		List<BIR> updatedBIRList = new ArrayList<>();
		JAXBContext jaxbContext = JAXBContext.newInstance(BIR.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		JAXBElement<BIR> jaxBir = unmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(xmlBytes)),
				BIR.class);
		BIR birRoot = jaxBir.getValue();
		for (BIR bir : birRoot.getBirs()) {
			if (type != null) {
				biometricType = getBiometricType(type);
				BDBInfo bdbInfo = bir.getBdbInfo();
				if (bdbInfo != null) {
					List<BiometricType> biometricTypes = bdbInfo.getType();
					if (biometricTypes != null && biometricTypes.contains(biometricType)) {
						updatedBIRList.add(bir);
					}
				}
			}
		}
		return updatedBIRList;
	}

	public static BiometricType getBiometricType(String type) {
		if (isInEnum(type, BiometricType.class)) {
			return BiometricType.valueOf(type);
		} else {
			if (type.equals("FMR"))
				return BiometricType.FINGER;
			else
				return BiometricType.fromValue(type);
		}
	}

	public static <E extends Enum<E>> boolean isInEnum(String value, Class<E> enumClass) {
		for (E e : enumClass.getEnumConstants()) {
			if (e.name().equals(value)) {
				return true;
			}
		}
		return false;
	}
}
