package robothealth.util;

import robothealth.exceptions.AccesNonAutoriseException;

/**
 * Service minimal de contrôle d'accès.
 * Il protège les missions sensibles à l'aide de codes distincts.
 */
public class SecurityService {
    private final String codePharmacie;
    private final String codePatient;

    public SecurityService(String codePharmacie, String codePatient) {
        this.codePharmacie = codePharmacie;
        this.codePatient = codePatient;
    }

    public void verifierCodePharmacie(String codeSaisi) throws AccesNonAutoriseException {
        if (codeSaisi == null || !codePharmacie.equals(codeSaisi.trim())) {
            throw new AccesNonAutoriseException("Code pharmacie invalide.");
        }
    }

    public void verifierCodePatient(String codeSaisi) throws AccesNonAutoriseException {
        if (codeSaisi == null || !codePatient.equals(codeSaisi.trim())) {
            throw new AccesNonAutoriseException("Code d'accès patient invalide.");
        }
    }
}
