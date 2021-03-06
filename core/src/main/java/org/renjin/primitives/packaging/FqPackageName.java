package org.renjin.primitives.packaging;

import org.renjin.sexp.Symbol;

/**
 * A fully-qualified package name that includes a
 * {@code groupId} as well as an {@code artifactName}
 */
public final class FqPackageName {

  public static final String CRAN_GROUP_ID = "org.renjin.cran";

  public static final String CORE_GROUP_ID = "org.renjin";


  public static final FqPackageName BASE = new FqPackageName("org.renjin", "base");

  private final String groupId;
  private final String packageName;


  public FqPackageName(String groupId, String packageName) {
    this.groupId = groupId;
    this.packageName = packageName;
  }

  /**
   * Constructs a fully-qualified name from a string in the form
   * {groupId}.{packageName}, for example: org.myGroup.myPackage
   */
  public static FqPackageName fromSymbol(Symbol symbol) {
    String name = symbol.getPrintName();
    int sep = name.lastIndexOf(':');
    if(sep == -1) {
      sep = name.lastIndexOf(".");
    }
    return new FqPackageName(
            name.substring(0, sep),
            name.substring(sep+1));
  }

  public static FqPackageName cranPackage(Symbol name) {
    return new FqPackageName(CRAN_GROUP_ID, name.getPrintName());
  }

  public static FqPackageName cranPackage(String name) {
    return new FqPackageName(CRAN_GROUP_ID, name);
  }

  public static FqPackageName corePackage(Symbol name) {
    return new FqPackageName(CORE_GROUP_ID, name.getPrintName());
  }

  public static FqPackageName corePackage(String name) {
    return new FqPackageName(CORE_GROUP_ID, name);
  }

  public String getGroupId() {
    return groupId;
  }

  public String getPackageName() {
    return packageName;
  }

  /**
   *
   * @return the local component of the package name as an R symbol
   */
  public Symbol getPackageSymbol() {
    return Symbol.get(packageName);
  }

  public String toString(char separator) {
    return groupId + separator + packageName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FqPackageName that = (FqPackageName) o;

    if (!groupId.equals(that.groupId)) return false;
    if (!packageName.equals(that.packageName)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = groupId.hashCode();
    result = 31 * result + packageName.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return toString(':');
  }

}
