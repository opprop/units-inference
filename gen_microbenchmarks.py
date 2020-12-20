import os
import argparse
import random

starting_var_count = 0
result_var_count = 0

def format_tabs(str, num_tabs = 2):
    return ' ' * num_tabs * 4 + str

# line of code of applying a binary operation (*, /, +, -) to all operands
def gen_op_line(operands, op, annotation = None):
    global result_var_count
    result_var_count += 1
    var_name = 'result' + str(result_var_count)
    code = ''
    if annotation is not None:
        code += format_tabs('@' + annotation + '\n')
    code += format_tabs('double ' + var_name + ' = ')
    if operands[0] == '1':    
        code += '(@Dimensionless int) '
    code += operands[0]
    for i in range(1, len(operands)):
        code += ' ' + op + ' ' + operands[i]
    code += ';\n'
    return var_name, code

# line of code containing a comparison
def gen_comp_line(left_operand, right_operand):
    return format_tabs('if (' + left_operand + ' == ' + right_operand + ') {}\n')

def make_units(amt, base_units):
    units = []
    idx = 0
    # try to evenly distribute among the base units
    for i in range(amt):
        units.append(base_units[idx])
        idx = (idx + 1) % len(base_units)
    return units

def make_variables(amt):
    global starting_var_count
    variables = []
    for i in range(amt):
        starting_var_count += 1
        variables.append('starting' + str(starting_var_count))
    return variables

# lines of code containing the initialization of starting variables, possibly annotated
def gen_initial_sequence(variables, units = None):
    code = ''
    for i in range(len(variables)):
        if units is not None:
            code += format_tabs('@' + units[i] + '\n')
        # everything is a double initialized to 0, since the type and value do not matter
        code += format_tabs('double ' + variables[i] + ' = 0;\n')
    return code

# create variables, initialize them, and annotate a portion of them
def gen_var_initialization(pct_annotated, amt, base_units):
    pct_annotated /= 100.0
    num_annotated_vars = int(pct_annotated * amt)
    variables = make_variables(amt)
    annotations = make_units(num_annotated_vars, base_units)
    code = gen_initial_sequence(variables[:num_annotated_vars], annotations)
    code += gen_initial_sequence(variables[num_annotated_vars:])
    return code, variables

# apply num_ops operations to some variables, possibly annotating the end result
def gen_op_sequence(op, num_ops, pct_annotated, base_units, per_line, corrected, end_annotation = None):
    num_vars = num_ops if corrected else num_ops + 1
    code, variables = gen_var_initialization(pct_annotated, num_vars, base_units)
    if corrected:
        variables.insert(0, '1')
    if per_line:
        prev_var, line = gen_op_line([variables[0], variables[1]], op)
        code += line
        for i in range(2, len(variables)):
            if i == len(variables) - 1 and end_annotation is not None:
                code += format_tabs('@' + end_annotation + '\n')
            # operations always use the previous variable as the left operand
            prev_var, line = gen_op_line([prev_var, variables[i]], op)
            code += line
    else:
        _, line = gen_op_line(variables, op, end_annotation)
        code += line
    return code

def gen_if_sequence(num_comps, pct_annotated, base_units, corrected):
    num_vars = num_comps if corrected else num_comps + 1
    code, variables = gen_var_initialization(pct_annotated, num_vars, base_units)
    if corrected:
        variables.insert(0, '1')
    for i in range(1, len(variables)):
        code += gen_comp_line(variables[i - 1], variables[i])
    return code
   
def generate_file(mult = 0, mult_groups = 1, mult_end = False, mult_perline = False, mult_nocorrection = False, mult_annot = 100, \
                  add = 0, add_groups = 1, add_end = False, add_perline = False, add_nocorrection = False, add_annot = 100, \
                  comp = 0, comp_groups = 1, comp_nocorrection = False, comp_annot = 100):

    global starting_var_count, result_var_count
    starting_var_count = 0
    result_var_count = 0
   
    directory = 'generated_microbenchmarks/'
    if not os.path.exists(directory):
        os.mkdir(directory)

    file_name = 'Mult{0}x{1}_Add{2}x{3}_Comp{4}x{5}'.format(mult_groups, mult, add_groups, add, comp_groups, comp)
    java_file_name = directory + file_name + '.java'
    
    with open(java_file_name, 'w') as f:
        code = 'import units.qual.*;\n\n'
        code += 'public class ' + file_name + ' {\n\n'
        code += format_tabs('public ' + file_name + '() {\n\n', 1)
 
        # arbitrarily chosen, the specific units do not matter
        base_units = ['m', 'g', 's', 'mPERs', 'mol', 'gPERmol', 'm2s2']
        
        if mult_groups * mult > 0:
            # arbitrarily annotate the end as meters, whether inference is successful does not matter
            end_annotation = 'm' if mult_end else None
            for i in range(mult_groups):
                code += format_tabs('// A group of {} multiplications\n'.format(mult))
                code += gen_op_sequence('*', mult, mult_annot, base_units, mult_perline, not mult_nocorrection, end_annotation = end_annotation)
                code += '\n'
        
        if add_groups * add > 0:
            for i in range(add_groups):
                code += format_tabs('// A group of {} additions\n'.format(add))
                # unlike multiplication, we cannot successfully add arbitrary units together, so just choose one
                chosen_unit = random.choice(base_units)
                end_annotation = chosen_unit if add_end else None
                code += gen_op_sequence('+', add, add_annot, [chosen_unit], add_perline, not add_nocorrection, end_annotation = end_annotation)
                code += '\n'
   
        if comp_groups * comp > 0:
            for i in range(comp_groups):
                code += format_tabs('// A group of {} comparisons\n'.format(comp))
                # similarly as addition, want to compare between units that are the same
                chosen_unit = [random.choice(base_units)]
                code += gen_if_sequence(comp, comp_annot, chosen_unit, not comp_nocorrection)
                code += '\n'
   
        code += format_tabs('}\n', 1)
        code += '}';
        f.write(code)
        print('Generated microbenchmark: ' + java_file_name)

    return java_file_name

if __name__ == "__main__":
   
    # command line arguments
    parser = argparse.ArgumentParser(description='This script generates microbenchmarks for PUnits (see docs for more details).')
   
    parser.add_argument('--mult', type=int, default=0, help='Number of multiplication constraints in each group')
    parser.add_argument('--mult-groups', type=int, default=1, help='Number of groups of multiplications')
    parser.add_argument('--mult-end', default=False, action='store_true', help='Specify to annotate the end variable of the multiplications in each group')
    parser.add_argument('--mult-perline', default=False, action='store_true', help='Specify to put one multiplication in each line and use intermediate variables to store results')
    parser.add_argument('--mult-nocorrection', default=False, action='store_true', help='Specify to disable using constants to ensure that the number of variables equal the number of multiplications')
    parser.add_argument('--mult-annot', type=int, default=100, help='Percentage of starting variables for multiplication in each group which should be annotated with some unit')

    parser.add_argument('--add', type=int, default=0, help='Number of addition constraints in each group')
    parser.add_argument('--add-groups', type=int, default=1, help='Number of groups of additions')
    parser.add_argument('--add-end', default=False, action='store_true', help='Specify to annotate the end variable of the additions in each group')
    parser.add_argument('--add-perline', default=False, action='store_true', help='Specify to put one addition in each line and use intermediate variables to store results')
    parser.add_argument('--add-nocorrection', default=False, action='store_true', help='Specify to disable using constants to ensure that the number of variables equal the number of additions')
    parser.add_argument('--add-annot', type=int, default=100, help='Percentage of starting variables for addition in each group which should be annotated with some unit')
   
    parser.add_argument('--comp', type=int, default=0, help='Number of comparison constraints in each group')
    parser.add_argument('--comp-groups', type=int, default=1, help='Number of groups of comparison constraints')
    parser.add_argument('--comp-nocorrection', default=True, action='store_false', help='Specify to disable using constants to ensure that the number of variables equal the number of comparisons')
    parser.add_argument('--comp-annot', type=int, default=100, help='Percentage of starting variables for comparison in each group which should be annotated with some unit')

    args = parser.parse_args()

    file_name = generate_file( \
        args.mult, args.mult_groups, args.mult_end, args.mult_perline, args.mult_nocorrection, args.mult_annot, \
        args.add, args.add_groups, args.add_end, args.add_perline, args.add_nocorrection, args.add_annot, \
        args.comp, args.comp_groups, args.comp_nocorrection, args.comp_annot \
    )
    
