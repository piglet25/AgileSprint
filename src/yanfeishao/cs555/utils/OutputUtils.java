package yanfeishao.cs555.utils;

import yanfeishao.cs555.constant.ErrorCode;
import yanfeishao.cs555.constant.ErrorInfo;
import yanfeishao.cs555.constant.FormatterRegex;
import yanfeishao.cs555.constant.KeywordsConstant;
import yanfeishao.cs555.entities.FamilyEntity;
import yanfeishao.cs555.entities.PersonEntity;
import yanfeishao.cs555.enums.ParseEnum;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Xiaonan Zhang on 9/18/15.
 * Refactor by Yanfei Shao on 9/20/15.
 */
public class OutputUtils {

    /**
     * Read all of the data of GEDCOM file
     * print the unique identifiers and names of the husbands and wives, in order by unique family identifiers and names
     * of each of the individuals in order by their unique identifiers.
     *
     * @param simpleDBUtils
     *         the simple DB utils
     * @param prefix
     *         the output prefix
     */
    public void outputResult(SimpleDBUtils simpleDBUtils, String prefix) {
        switch (prefix) {
            case KeywordsConstant.INDI: {
                System.out.println(String.format(FormatterRegex.PERSON_TABLE_TITLE, KeywordsConstant.IDENTIFIER, KeywordsConstant.NAME));
                for (PersonEntity personEntity : simpleDBUtils.getPersonDBList()) {
                    try {
                        System.out.println(String.format(FormatterRegex.PERSON_TABLE_DATA, personEntity.getIdentifier(), personEntity.getName()));
                    } catch (NullPointerException npe) {
                        continue;
                    }
                }
            }
            break;
            case KeywordsConstant.FAM: {
                System.out.println(String.format(FormatterRegex.FAMILY_TABLE_TITLE, KeywordsConstant.IDENTIFIER, KeywordsConstant.HUSBAND_NAME, KeywordsConstant.WIFE_NAME));
                for (FamilyEntity familyEntity : simpleDBUtils.getFamilyDBList()) {
                    System.out.println(String.format(FormatterRegex.FAMILY_TABLE_DATA, familyEntity.getIdentifier(), familyEntity.getFather().getName(), familyEntity.getMother().getName()));
                }
            }
            break;
        }
        System.out.println();
    }

    /**
     * Read all of the data of GEDCOM file
     * print the error information based on the US case description
     *
     * @param simpleDBUtils
     *         the simple DB utils
     * @param prefix
     *         the output prefix
     */
    public void outputError(SimpleDBUtils simpleDBUtils, String prefix) {
        System.out.println(String.format(FormatterRegex.ERROR_TITLE, KeywordsConstant.ERROR, prefix));
        Set<String> results = parseError(simpleDBUtils, prefix);
        results.forEach((result -> System.out.println(result)));
        System.out.println();
    }

    /**
     * Parse error.
     *
     * @param simpleDBUtils
     *         the simple dB utils
     * @param prefix
     *         the prefix
     *
     * @return the set
     */
    public Set<String> parseError(SimpleDBUtils simpleDBUtils, String prefix) {
        Set<String> result = new HashSet<>();
        switch (prefix) {
            case ErrorCode.US01:
                break;
            case ErrorCode.US02: {
                // Birth before marriage
                simpleDBUtils.getFamilyDBList().forEach((familyEntity -> {
                    Date marriageDate = familyEntity.getMarriedDate();
                    Date husbandBirthDate = familyEntity.getFather().getBirthDate();
                    Date wifeBirthDate = familyEntity.getMother().getBirthDate();
                    if (marriageDate != null && husbandBirthDate != null && wifeBirthDate != null) {
                        if (husbandBirthDate.after(marriageDate) || wifeBirthDate.after(marriageDate)) {
                            result.add(String.format(FormatterRegex.ERROR_FAMILY, familyEntity.getIdentifier(), ErrorInfo.US02));
                        }
                    }
                }));
            }
            break;
            case ErrorCode.US03:
                break;
            case ErrorCode.US04: {
                // Divorce before marriage
                simpleDBUtils.getFamilyDBList().forEach((familyEntity -> {
                    Date marriageDate = familyEntity.getMarriedDate();
                    Date divorceDate = familyEntity.getDivorceDate();
                    if (marriageDate != null && divorceDate != null) {
                        if (marriageDate.after(divorceDate) || divorceDate.before(marriageDate)) {
                            result.add(String.format(FormatterRegex.ERROR_FAMILY, familyEntity.getIdentifier(), ErrorInfo.US04));
                        }
                    }
                }));
            }
            break;
            case ErrorCode.US05: {
                // Marriage before death
                simpleDBUtils.getFamilyDBList().forEach((familyEntity -> {
                    Date marriageDate = familyEntity.getMarriedDate();
                    Date husbandDeathDate = familyEntity.getFather().getDeathDate();
                    Date wifeDeathDate = familyEntity.getMother().getDeathDate();
                    if (marriageDate != null && husbandDeathDate != null && wifeDeathDate != null) {
                        if (husbandDeathDate.before(marriageDate) || wifeDeathDate.before(marriageDate)) {
                            result.add(String.format(FormatterRegex.ERROR_FAMILY, familyEntity.getIdentifier(), ErrorInfo.US05));
                        }
                    }
                }));
            }
            break;
            case ErrorCode.US06:{
                // Divorce after death of both spouses
                simpleDBUtils.getFamilyDBList().forEach((familyEntity -> {
                    Date divorceDate = familyEntity.getDivorceDate();
                    Date husbandDeathDate = familyEntity.getFather().getDeathDate();
                    Date wifeDeathDate = familyEntity.getMother().getDeathDate();
                    if (divorceDate != null && husbandDeathDate != null && wifeDeathDate != null) {
                        if (husbandDeathDate.before(divorceDate) || wifeDeathDate.before(divorceDate)) {
                            result.add(String.format(FormatterRegex.ERROR_PERSON, familyEntity.getIdentifier(), ErrorInfo.US06));
                        }
                    }
                }));
            }

        }
        return result;
    }

    private String[] splitName(String name) {
        String[] splitResult = name.split(ParseEnum.PERSON_NAME_SPLIT.toString());
        return splitResult;
    }
}
