import os
import argparse
import random

var_count = 0

# add tabs so that the generated Java file looks nice
def add_tabs(str, num_tabs):
    return ' ' * num_tabs * 4 + str

# create a new variable
def next_var():
    global var_count
    var_count += 1
    var_name = 'var' + str(var_count)
    return var_name

# line of code containing a binary operation (*, /, +, -)
def gen_op_line(left_operand, right_operand, op, num_tabs):
    var_name = next_var()
    # every line causes the initialization of a new variable to store the intermediate result
    line = add_tabs('double ' + var_name + ' = ' + left_operand + ' ' + op + ' ' + right_operand + ';\n', num_tabs)
    return var_name, line

# line of code containing a comparison
def gen_comp_line(left_operand, right_operand, num_tabs):
    line = add_tabs('if (' + left_operand + ' == ' + right_operand + ') {}\n', num_tabs)
    return line

def create_annotated_variables(amt, base_units):
    variables = []
    units = []
    # evenly distribute among the available units as best as possible
    for base_unit in base_units:
        for i in range(int(amt / len(base_units))):
            variables.append(next_var())
            units.append(base_unit)
    left_over = int(amt % len(base_units))
    for i in range(left_over):
        variables.append(next_var())
        units.append(base_units[i])
    return variables, units

def create_unannotated_variables(amt):
    return [next_var() for i in range(amt)]

# initial sequence of starting variables, possibly annotated
def gen_initial_sequence(variables, units = None, num_tabs = 2):
    code = ''
    for i in range(len(variables)):
        if units is not None:
            code += add_tabs('@' + units[i] + '\n', num_tabs)
        # everything is a double initialized to 0, since the type/value does not matter
        code += add_tabs('double ' + variables[i] + ' = 0;\n', num_tabs)
    return code

# create k variables, initialize them, and annotate a portion of them
def make_variables(pct_annotated, k, base_units, num_tabs = 2):
    num_annotated_vars = int(pct_annotated * k)
    num_other_vars = k - num_annotated_vars
    
    # a list of tuples of variable names and their unit (one of the ones in base_units)
    annotated_variables, units = create_annotated_variables(num_annotated_vars, base_units)
    code = gen_initial_sequence(annotated_variables, units)
    other_variables = create_unannotated_variables(num_other_vars)
    code += gen_initial_sequence(other_variables)
    return code, annotated_variables + other_variables

# apply num_ops operations to some variables, possibly annotating the end result
def gen_op_sequence(op, num_ops, pct_annotated, base_units, end_annotation = None, num_tabs = 2):
    code, variables = make_variables(pct_annotated, num_ops + 1, base_units)
    prev_var, line = gen_op_line(variables[0], variables[1], op, num_tabs)
    code += line
    for i in range(2, len(variables)):
        if i == len(variables) - 1 and end_annotation is not None:
            code += add_tabs('@' + end_annotation + '\n', num_tabs)
        # operations always use the previous variable as the left operand
        prev_var, line = gen_op_line(prev_var, variables[i], op, num_tabs)
        code += line
    return code

def gen_if_sequence(num_comps, pct_annotated, base_units, num_tabs = 2):
    code, variables = make_variables(pct_annotated, num_comps + 1, base_units)
    for i in range(1, len(variables)):
        code += gen_comp_line(variables[i - 1], variables[i], num_tabs)
    return code
   

if __name__ == "__main__":
   
    # command line parsing
    parser = argparse.ArgumentParser(description='This script generates micro benchmarks for PUnits.')
    parser.add_argument('--mult', type=int, default=0, help='Number of multiplication constraints in each group')
    parser.add_argument('--multgroups', type=int, default=1, help='Number of groups of multiplication constraints')
    parser.add_argument('--multend', type=bool, default=True, help='Whether the end variable of the mult sequence in each group should be annotated')
    parser.add_argument('--add', type=int, default=0, help='Number of addition constraints in each group')
    parser.add_argument('--addgroups', type=int, default=1, help='Number of groups of addition constraints')
    parser.add_argument('--addend', type=bool, default=True, help='Whether the end variable of the add sequence in each group should be annotated')
    parser.add_argument('--comp', type=int, default=0, help='Number of comparison constraints in each group')
    parser.add_argument('--compgroups', type=int, default=1, help='Number of groups of comparison constraints')
    parser.add_argument('--annot', type=int, default=100, help='Percentage of starting variables that should be annotated with some unit')

    args = parser.parse_args()
    
    # make the java file
    file_name = 'Test_Mult{0}x{1}_Add{2}x{3}_Comp{4}x{5}'.format(args.multgroups, args.mult, args.addgroups, args.add, args.compgroups, args.comp)
    java_file_name = file_name + ".java"
    with open(java_file_name, 'w') as f:
        code = 'import units.qual.*;\n\n'
        code += 'public class ' + file_name + ' {\n\n'
        code += add_tabs('public static void main(String[] args) {\n\n', 1)

        # arbitrarily chosen
        base_units = ['m', 'g', 's', 'mPERs', 'mol', 'gPERmol', 'm2s2']
    
        if args.multgroups * args.mult > 0:
            # simply annotating the end as meters may or may not cause successful inference
            end_annotation = 'm' if args.multend else None
            for i in range(args.multgroups):
                code += add_tabs('// A sequence of {} multiplications\n'.format(args.mult), 2)
                code += gen_op_sequence('*', args.mult, (args.annot / 100.0), base_units, end_annotation = end_annotation)
                code += '\n'
  
        if args.addgroups * args.add > 0:
            for i in range(args.addgroups):
                code += add_tabs('// A sequence of {} additions\n'.format(args.add), 2)
                # unlike multiplication, we cannot multiply arbitrary units together and instead,
                # need to choose one, to annotate the end result and also some starting variables
                chosen_unit = random.choice(base_units)
                end_annotation = chosen_unit if args.addend else None
                code += gen_op_sequence('+', args.add, (args.annot / 100.0), [chosen_unit], end_annotation = end_annotation)
                code += '\n'
    
        if args.compgroups * args.comp > 0:
            for i in range(args.compgroups):
                code += add_tabs('// A sequence of {} comparisons\n'.format(args.comp), 2)
                # similarly as addition, want to compare between units that are the same
                chosen_unit = [random.choice(base_units)]
                code += gen_if_sequence(args.comp, (args.annot / 100.0), chosen_unit)
   
        code += add_tabs('}\n', 1)
        code += '}';

        f.write(code)

        print('Generated code at ' + java_file_name)

        f.close()

