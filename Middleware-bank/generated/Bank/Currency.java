/**
 * Autogenerated by Thrift Compiler (0.12.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package Bank;


@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.12.0)", date = "2019-05-04")
public enum Currency implements org.apache.thrift.TEnum {
  EUR(0),
  USD(1),
  GBP(2),
  PLN(3);

  private final int value;

  private Currency(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  @org.apache.thrift.annotation.Nullable
  public static Currency findByValue(int value) { 
    switch (value) {
      case 0:
        return EUR;
      case 1:
        return USD;
      case 2:
        return GBP;
      case 3:
        return PLN;
      default:
        return null;
    }
  }
}
