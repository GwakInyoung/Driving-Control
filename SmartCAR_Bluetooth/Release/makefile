################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include subdir.mk
-include objects.mk

ifneq ($(MAKECMDGOALS),clean)
ifneq ($(strip $(C++_DEPS)),)
-include $(C++_DEPS)
endif
ifneq ($(strip $(C_DEPS)),)
-include $(C_DEPS)
endif
ifneq ($(strip $(ASM_DEPS)),)
-include $(ASM_DEPS)
endif
ifneq ($(strip $(CC_DEPS)),)
-include $(CC_DEPS)
endif
ifneq ($(strip $(CPP_DEPS)),)
-include $(CPP_DEPS)
endif
ifneq ($(strip $(S_DEPS)),)
-include $(S_DEPS)
endif
ifneq ($(strip $(CXX_DEPS)),)
-include $(CXX_DEPS)
endif
ifneq ($(strip $(C_UPPER_DEPS)),)
-include $(C_UPPER_DEPS)
endif
ifneq ($(strip $(S_UPPER_DEPS)),)
-include $(S_UPPER_DEPS)
endif
endif

-include ../makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 
LSS += \
SmartCAR_Bluetooth.lss \

FLASH_IMAGE += \
SmartCAR_Bluetooth.hex \

EEPROM_IMAGE += \
SmartCAR_Bluetooth.eep \

SIZEDUMMY += \
sizedummy \


# All Target
all: SmartCAR_Bluetooth.elf secondary-outputs

# Tool invocations
SmartCAR_Bluetooth.elf: $(OBJS) $(USER_OBJS)
	@echo 'Building target: $@'
	@echo 'Invoking: AVR C++ Linker'
	avr-gcc -Os -Wl,--gc-sections  -L"C:\AndroXStudio\workspace\Arduino_Mega_2560_or_Mega_ADK/Release" -mmcu=atmega2560  -o"SmartCAR_Bluetooth.elf" $(OBJS) $(USER_OBJS) $(LIBS) -lm
	@echo 'Finished building target: $@'
	@echo ' '

SmartCAR_Bluetooth.lss: SmartCAR_Bluetooth.elf
	@echo 'Invoking: AVR Create Extended Listing'
	-avr-objdump -h -S SmartCAR_Bluetooth.elf  >"SmartCAR_Bluetooth.lss"
	@echo 'Finished building: $@'
	@echo ' '

SmartCAR_Bluetooth.hex: SmartCAR_Bluetooth.elf
	@echo 'Create Flash image (ihex format)'
	-avr-objcopy -R .eeprom -O ihex SmartCAR_Bluetooth.elf  "SmartCAR_Bluetooth.hex"
	@echo 'Finished building: $@'
	@echo ' '

SmartCAR_Bluetooth.eep: SmartCAR_Bluetooth.elf
	@echo 'Create eeprom image (ihex format)'
	-avr-objcopy -j .eeprom --no-change-warnings --change-section-lma .eeprom=0 -O ihex SmartCAR_Bluetooth.elf  "SmartCAR_Bluetooth.eep"
	@echo 'Finished building: $@'
	@echo ' '

sizedummy: SmartCAR_Bluetooth.elf
	@echo 'Invoking: Print Size'
	-avr-size --format=avr --mcu=atmega2560 SmartCAR_Bluetooth.elf
	@echo 'Finished building: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(OBJS)$(C_DEPS)$(EEPROM_IMAGE)$(ELFS)$(FLASH_IMAGE)$(LSS)$(S_DEPS)$(CXX_DEPS)$(S_UPPER_DEPS)$(C++_DEPS)$(ASM_DEPS)$(CC_DEPS)$(CPP_DEPS)$(C_UPPER_DEPS)$(SIZEDUMMY) SmartCAR_Bluetooth.elf
	-@echo ' '

secondary-outputs: $(LSS) $(FLASH_IMAGE) $(EEPROM_IMAGE) $(SIZEDUMMY)

.PHONY: all clean dependents
.SECONDARY:

-include ../makefile.targets
